package com.sprint.mission.discodeit.entity;


public class Channel extends BaseEntity {
    private String name;
    private String description;
    private ChannelType type;

    public Channel(String name,String description,ChannelType type){
        this.name = name;
        this.description = description;
        this.type = type;
    }

    // temp ToString
    @Override
    public String toString(){
        String res = " ===== Channel ====== \n"
                + "id : "  + this.getId() + "\n"
                + "createdAt : " + this.getCreatedAt() + "\n"
                + "updatedAt :" + this.getUpdatedAt() + "\n"
                + "name : " + this.name + "\n"
                + "description :" + this.description + "\n"
                + "type : " + this.type + "\n";
        return res;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public ChannelType getType() {
        return type;
    }
    public void setType(ChannelType type) {
        this.type = type;
    }
}