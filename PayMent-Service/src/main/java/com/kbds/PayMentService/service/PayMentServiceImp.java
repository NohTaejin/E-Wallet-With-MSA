package com.kbds.PayMentService.service;

import com.example.ewallet.vo.ResponseEwallet;
import com.kbds.PayMentService.client.EwalletServiceClient;
import com.kbds.PayMentService.dto.PayMentDto;
import com.kbds.PayMentService.jpa.PayMentEntity;
import com.kbds.PayMentService.jpa.PayMentRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@Service
public class PayMentServiceImp implements PayMentService{
    PayMentRepository payMentRepository;

    EwalletServiceClient EwalletServiceClient;

    @Autowired
    public PayMentServiceImp(PayMentRepository payMentRepository, EwalletServiceClient EwalletServiceClient) {
        this.payMentRepository = payMentRepository;
        this.EwalletServiceClient = EwalletServiceClient;
    }

    @Override
    public PayMentDto createPayMent(PayMentDto paymentDto) {
        ResponseEntity<ResponseEwallet> responseEwallet = EwalletServiceClient.getSearchEwallet(paymentDto.getSendId());
        paymentDto.setEwalletId(responseEwallet.getBody().getEwalletId());
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        PayMentEntity payMentEntity = mapper.map(paymentDto, PayMentEntity.class);
        log.info("payMentEntity : " + payMentEntity.toString());
        payMentRepository.save(payMentEntity);

        PayMentDto returnPayMentDto = mapper.map(payMentEntity, PayMentDto.class);
        return returnPayMentDto;
    }

    @Override
    public Iterable<PayMentEntity> getReceivePayList(String receiveId) {
        return payMentRepository.findByReceiveId(receiveId);
    }

    @Override
    public String receivePayMent(PayMentDto payMentDto) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        PayMentEntity payMentEntity = mapper.map(payMentDto, PayMentEntity.class);

        payMentEntity = payMentRepository.findByUseId(payMentEntity.getUseId());

        if (payMentEntity != null) {
            // 전자지갑 잔액 UPDATE
            // 입출금이력 정보 UPDATE
            // 임시 송금테이블 삭제
            payMentRepository.deleteById(payMentEntity.getUseId());

        }

        return "송금이 완료 되었습니다.";
    }
}
