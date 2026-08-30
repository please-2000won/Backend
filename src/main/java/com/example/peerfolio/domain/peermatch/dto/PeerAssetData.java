package com.example.peerfolio.domain.peermatch.dto;

public record PeerAssetData(
        Long userId,
        Long depositBondAmount,
        Long domesticStockAmount,
        Long foreignStockAmount,
        Long alternativeAmount
) {

}
