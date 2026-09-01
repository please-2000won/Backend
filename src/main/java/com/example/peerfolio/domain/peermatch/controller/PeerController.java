package com.example.peerfolio.domain.peermatch.controller;

import com.example.peerfolio.domain.peermatch.dto.PeerCardResponse;
import com.example.peerfolio.domain.peermatch.dto.PeerComparisonResponse;
import com.example.peerfolio.domain.peermatch.service.PeerQueryService;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.global.apiPayload.ApiResponse;
import com.example.peerfolio.global.apiPayload.code.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/peers")
@Tag(
        name = "Peer",
        description = "피어 카드 및 금융정보 상세 비교 API"
)
public class PeerController {

    private final PeerQueryService peerQueryService;

    @GetMapping("/random")
    public ApiResponse<List<PeerCardResponse>> getRandomPeers(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "3") int size
    ) {
        List<PeerCardResponse> response =
                peerQueryService.getRandomPeers(
                        user.getId(),
                        size
                );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK, response
        );
    }

    @GetMapping("/{peerUserId}")
    @Operation(
            summary = "선택 피어와 내 금융정보 비교",
            description = """
                JWT 인증 사용자와 현재 피어 그룹에 포함된
                선택 피어의 경제적 여건 및 투자자산을 비교합니다.
                """
    )
    public ApiResponse<PeerComparisonResponse> getPeerComparison(
            @AuthenticationPrincipal User user,
            @PathVariable Long peerUserId
    ) {
        PeerComparisonResponse response =
                peerQueryService.getPeerComparison(
                        user,
                        peerUserId
                );

        return ApiResponse.onSuccess(
                GeneralSuccessCode.OK, response
        );
    }
}
