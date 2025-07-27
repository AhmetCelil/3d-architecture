package com.example.commerce.basedtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppMessageDto  implements Serializable {
    private String code;
    private String text;
    private AppMessageType type;

    @JsonIgnore
    private String[] args;

    public static AppMessageDtoBuilder builder() {
        return new AppMessageDtoBuilder();
    }

    public static class AppMessageDtoBuilder {
        private String code;
        private String text;
        private AppMessageType type;
        private String[] args;

        public AppMessageDtoBuilder code(String code) {
            this.code = code;
            return this;
        }

        public AppMessageDtoBuilder text(String text) {
            this.text = text;
            return this;
        }

        public AppMessageDtoBuilder type(AppMessageType type) {
            this.type = type;
            return this;
        }

        @JsonIgnore
        public AppMessageDtoBuilder args(String[] args) {
            this.args = args;
            return this;
        }

        public AppMessageDto build() {
            return new AppMessageDto(this.code, this.text, this.type, this.args);
        }

        @Override
        public String toString() {
            return "AppMessageDto.AppMessageDtoBuilder(code=" + code +
                    ", text=" + text +
                    ", type=" + type +
                    ", args=" + Arrays.deepToString(args) + ")";
        }
    }
}
