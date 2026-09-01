package com.example.peerfolio.domain.peermatch.controller;

import com.example.peerfolio.domain.peermatch.dto.PeerCardResponse;
import com.example.peerfolio.domain.peermatch.service.PeerQueryService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/peers")
public class PeerController {

    private final PeerQueryService peerQueryService;

    @GetMapping("/random")
    public ApiResponse<List<PeerCardResponse>> getRandomPeers(
            @AuthenticationPrincipal User user
    ) {
        List<PeerCardResponse> response =
                peerQueryService.getRandomPeers(user.getId());

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK, response
        );
    }


}
