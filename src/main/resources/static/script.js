// API endpoints
const API_BASE_URL = '/api';
const ENDPOINTS = {
    USERS: `${API_BASE_URL}/user/findAll`,
    BINARY_CONTENT: `${API_BASE_URL}/binaryContent/find`
};

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    fetchAndRenderUsers();
});

// Fetch users from the API
async function fetchAndRenderUsers() {
    try {
        const response = await fetch(ENDPOINTS.USERS);
        if (!response.ok) throw new Error('Failed to fetch users');
        const users = await response.json();
        console.log("받아온 유저 데이터:", users); // 브라우저 콘솔 확인용
        renderUserList(users);
    } catch (error) {
        console.error('Error fetching users:', error);
    }
}

// Fetch user profile image
async function fetchUserProfile(profileId) {
    // profileId가 비어있다면 아예 요청을 날리지 않고 바로 기본 이미지 리턴
    if (!profileId) {
        return '/default-avatar.png';
    }
    try {
        const response = await fetch(`${ENDPOINTS.BINARY_CONTENT}?binaryContentId=${profileId}`);
        if (!response.ok) throw new Error('Failed to fetch profile');
        const profile = await response.json();

        // Convert base64 encoded bytes to data URL
        return `data:${profile.contentType};base64,${profile.bytes}`;
    } catch (error) {
        console.error('Error fetching profile:', error);
        return '/default-avatar.png'; // Fallback to default avatar
    }
}

// Render user list
async function renderUserList(users) {
    const userListElement = document.getElementById('userList');
    if (!userListElement) return;

    userListElement.innerHTML = ''; // Clear existing content

    if (!users || users.length === 0) {
        userListElement.innerHTML = '<div style="padding: 20px; text-align: center; color: #666;">등록된 사용자가 없습니다.</div>';
        return;
    }

    for (const user of users) {
        try {
            // 🎯 방어적 코드: profileId가 null이거나 undefined, 혹은 문자열 'null'인 경우까지 철저하게 체크
            const hasProfile = user.profileId && user.profileId !== 'null';

            const profileUrl = hasProfile ?
                await fetchUserProfile(user.profileId) :
                '/default-avatar.png';

            // 백엔드 DTO 스펙(isOnline 또는 online) 둘 다 대응할 수 있도록 처리
            const isUserOnline = user.isOnline || user.online || false;

            const userElement = document.createElement('div');
            userElement.className = 'user-item';
            userElement.innerHTML = `
                <img src="${profileUrl}" alt="${user.username || 'Unknown'}" class="user-avatar">
                <div class="user-info">
                    <div class="user-name">${user.username || '이름 없음'}</div>
                    <div class="user-email">${user.email || '-'}</div>
                </div>
                <div class="status-badge ${isUserOnline ? 'online' : 'offline'}">
                    ${isUserOnline ? '온라인' : '오프라인'}
                </div>
            `;

            userListElement.appendChild(userElement);
        } catch (itemError) {
            // 특정 유저 한 명을 그리다 에러가 나도, 전체 화면이 먹통이 되지 않도록 멈춤 방지
            console.error("유저 렌더링 중 에러 발생:", itemError);
        }
    }
}