package com.example.commerce.basedtos;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AppMessageDto {

    private String code;
    private String text;
    private AppMessageType type;
    private String[] args;

    public AppMessageDto(String code, String text, AppMessageType type, String[] args) {
        this.code = code;
        this.text = text;
        this.type = type;
        this.args = args;
    }

    public AppMessageDto(String code, String text, Object... args) {
        this.code = code;
        this.text = text;
        this.args = convertArgs(args);
        this.type = AppMessageType.ERROR; // varsayılan
    }

    private String[] convertArgs(Object[] args) {
        if (args == null) return new String[0];
        String[] stringArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            stringArgs[i] = args[i].toString();
        }
        return stringArgs;
    }

    // getter / setter / builder vs.
}
