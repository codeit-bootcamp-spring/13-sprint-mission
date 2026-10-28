// 빌드된 UI와의 연결 지점은 assets/index-DB4IjbRs.js 마지막 한 줄이다.
// 프론트 원본이 추가되면 이 모듈을 원본 진입점에서 import한다.
export function installRealtime({auth, channels, users, messages, binaries, notifications, download}) {
  let abort, socket, retry, socketRetry, lastEventId, currentToken, activeChannel;
  let frameBuffer = '';
  const upsert = (store, field, value, deleted = false) => store.setState(state => ({
    [field]: deleted ? state[field].filter(item => item.id !== value.id)
      : state[field].some(item => item.id === value.id)
        ? state[field].map(item => item.id === value.id ? value : item)
        : [value, ...state[field]]
  }));

  async function onEvent(name, value) {
    if (name === 'notifications.created') upsert(notifications, 'notifications', value);
    else if (name.startsWith('channels.')) {
      upsert(channels, 'channels', value, name.endsWith('.deleted'));
      if (name.endsWith('.deleted') && value.id === activeChannel) selectChannel(null);
    } else if (name.startsWith('users.')) upsert(users, 'users', value, name.endsWith('.deleted'));
    else if (name === 'binaryContents.updated') {
      let extra = {};
      if (value.status === 'SUCCESS') {
        const result = await download(value.id);
        const url = URL.createObjectURL(result.blob);
        binaries.getState().binaryContents[value.id]?.revokeUrl?.();
        extra = {url, revokeUrl: () => URL.revokeObjectURL(url)};
      }
      binaries.setState(state => ({binaryContents: {...state.binaryContents,
        [value.id]: {...state.binaryContents[value.id], ...value, ...extra}}}));
    }
  }

  async function connectSse(token) {
    abort = new AbortController();
    const signal = abort.signal;
    try {
      const response = await fetch('/api/sse', {
        headers: {Authorization: `Bearer ${token}`, ...(lastEventId ? {'Last-Event-ID': lastEventId} : {})},
        signal
      });
      if (!response.ok) throw new Error(`SSE HTTP ${response.status}`);
      // 재시작/보관 만료로 복원할 수 없는 구간은 REST 스냅샷으로 보완한다.
      const user = auth.getState().currentUser;
      await Promise.all([users.getState().fetchUsers(), notifications.getState().fetchNotifications(),
        channels.getState().fetchChannels(user.id)]);
      const reader = response.body.pipeThrough(new TextDecoderStream()).getReader();
      let buffer = '';
      while (true) {
        const {value, done} = await reader.read();
        if (done) break;
        buffer += value.replace(/\r/g, '');
        let boundary;
        while ((boundary = buffer.indexOf('\n\n')) >= 0) {
          const block = buffer.slice(0, boundary); buffer = buffer.slice(boundary + 2);
          let id, name, data = [];
          for (const line of block.split('\n')) {
            if (line.startsWith('id:')) id = line.slice(3).trim();
            if (line.startsWith('event:')) name = line.slice(6).trim();
            if (line.startsWith('data:')) data.push(line.slice(5).trimStart());
          }
          if (name && data.length) {
            await onEvent(name, JSON.parse(data.join('\n')));
            if (id) lastEventId = id;
          }
        }
      }
    } catch (error) {
      if (!signal.aborted) console.error('SSE 연결 종료', error);
    } finally {
      if (!signal.aborted && currentToken === token) retry = setTimeout(() => connectSse(token), 3000);
    }
  }

  const sendFrame = (command, headers = {}, body = '') => {
    if (socket?.readyState !== WebSocket.OPEN) return false;
    const frame = command + '\n' + Object.entries(headers).map(([k,v]) => `${k}:${v}`).join('\n')
      + '\n\n' + body + '\0';
    socket.send(JSON.stringify([frame]));
    return true;
  };
  let connected = false, subscription = 0;
  function subscribe() {
    if (connected && activeChannel) {
      sendFrame('SUBSCRIBE', {id: `channel-${subscription}`, destination: `/sub/channels.${activeChannel}.messages`});
    }
  }
  function selectChannel(id) {
    if (connected && activeChannel) sendFrame('UNSUBSCRIBE', {id: `channel-${subscription}`});
    subscription++; activeChannel = id; subscribe();
  }
  function connectSocket(token) {
    connected = false;
    const session = crypto.randomUUID().replaceAll('-', '');
    const ws = new WebSocket(`${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/ws/000/${session}/websocket`);
    socket = ws; frameBuffer = '';
    ws.onmessage = event => {
      if (event.data === 'o') {
        sendFrame('CONNECT', {Authorization: `Bearer ${token}`, 'accept-version': '1.2', 'heart-beat': '0,0'});
      } else if (event.data.startsWith('a')) {
        for (const chunk of JSON.parse(event.data.slice(1))) {
          frameBuffer += chunk;
          let end;
          while ((end = frameBuffer.indexOf('\0')) >= 0) {
            const frame = frameBuffer.slice(0, end).replace(/^\n+/, '');
            frameBuffer = frameBuffer.slice(end + 1);
            if (frame.startsWith('CONNECTED')) {
              connected = true; subscribe();
              if (activeChannel) messages.getState().fetchMessages(activeChannel, null);
            }
            else if (frame.startsWith('MESSAGE')) {
              const message = JSON.parse(frame.slice(frame.indexOf('\n\n') + 2));
              if (message.channelId === activeChannel) upsert(messages, 'messages', message);
            } else if (frame.startsWith('ERROR')) { console.error('STOMP 오류', frame); ws.close(); }
          }
        }
      }
    };
    ws.onclose = () => {
      connected = false;
      if (currentToken === token) socketRetry = setTimeout(() => connectSocket(token), 3000);
    };
  }

  // 기존 화면의 startPolling 호출은 실시간 구독으로 연결한다.
  channels.setState({startPolling: () => {}, stopPolling: () => {}});
  binaries.setState({startPolling: () => {}});
  const originalCreate = messages.getState().createMessage;
  messages.setState({
    startPolling: id => selectChannel(id),
    stopPolling: id => { if (activeChannel === id) selectChannel(null); },
    createMessage: async (request, attachments) => {
      if (attachments?.length) return originalCreate(request, attachments);
      if (!connected) throw new Error('메시지 서버에 재연결 중입니다. 잠시 후 다시 전송하세요.');
      sendFrame('SEND', {destination: '/pub/messages', 'content-type': 'application/json'}, JSON.stringify(request));
      return request;
    }
  });
  auth.subscribe(state => {
    if (state.accessToken === currentToken) return;
    currentToken = state.accessToken;
    abort?.abort(); clearTimeout(retry); clearTimeout(socketRetry);
    if (socket) { socket.onclose = null; socket.close(); }
    connected = false;
    if (currentToken) { connectSse(currentToken); connectSocket(currentToken); }
    else { lastEventId = null; activeChannel = null; }
  });
}
