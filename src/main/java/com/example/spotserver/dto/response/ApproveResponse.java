package com.example.spotserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApproveResponse {

    private Long locationId;
    private Boolean approve;

}
