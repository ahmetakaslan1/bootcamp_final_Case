package com.akaslan.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Sistemin standart API yanıt formatı")
public class ApiResponse<T> {

    @Schema(description = "İşlemin başarılı olup olmadığı durumu", example = "true")
    private final boolean success;
    
    @Schema(description = "Kullanıcıya gösterilecek veya loglanacak mesaj", example = "İşlem başarılı")
    private final String message;
    
    @Schema(description = "İşlem sonucunda dönen ana veri (Eğer varsa)")
    private final T data;
    
    @Schema(description = "Hata durumunda dönen hata kodu", example = "VALIDATION_FAILED")
    private final String errorCode;
    
    @Schema(description = "Yanıtın oluşturulduğu zaman damgası")
    private final Instant timestamp;

    private ApiResponse(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }

    public static <T> ApiResponse<T> errorWithData(String message, String errorCode, T data) {
        return new ApiResponse<>(false, message, data, errorCode);
    }
}
