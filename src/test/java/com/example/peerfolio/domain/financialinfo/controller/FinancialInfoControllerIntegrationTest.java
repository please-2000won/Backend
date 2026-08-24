package com.example.peerfolio.domain.financialinfo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.peerfolio.domain.financialasset.repository.FinancialAssetRepository;
import com.example.peerfolio.domain.financialprofile.repository.FinancialProfileRepository;
import com.example.peerfolio.domain.user.entity.User;
import com.example.peerfolio.domain.user.repository.UserRepository;
import com.example.peerfolio.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FinancialInfoControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FinancialProfileRepository financialProfileRepository;

	@Autowired
	private FinancialAssetRepository financialAssetRepository;

	@Autowired
	private JwtProvider jwtProvider;

	private User user;
	private String authorization;

	@BeforeEach
	void setUp() {
		// given: 테스트마다 금융 정보 저장 상태를 초기화하고 인증 사용자를 새로 생성한다.
		financialAssetRepository.deleteAll();
		financialProfileRepository.deleteAll();
		userRepository.deleteAll();

		user = userRepository.save(User.create(
				"사용자",
				"financial-info@example.com",
				"encoded-password",
				"테스트닉네임001"
		));
		authorization = "Bearer " + jwtProvider.createAccessToken(user.getId());
	}

	@Test
	void financialInfoApiReturnsUnauthorizedWhenTokenIsMissing() throws Exception {
		mockMvc.perform(get("/api/v1/financial-info"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON401"));
	}

	@Test
	void getFinancialInfoFailsWhenFinancialInfoDoesNotExist() throws Exception {
		// when & then: 금융 정보를 입력하지 않은 사용자가 조회하면 404를 응답한다.
		mockMvc.perform(get("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON404"));
	}

	@Test
	void putAndGetFinancialInfoWorks() throws Exception {
		// when: 금융 정보를 최초 저장한다.
		mockMvc.perform(put("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "financialProfile": {
								    "age": 24,
								    "monthlyIncome": 2500000,
								    "fixedExpense": 900000,
								    "savingsGoal": 700000,
								    "totalAssetAmount": 12000000,
								    "totalDebtAmount": 3000000
								  },
								  "financialAsset": {
								    "depositBondAmount": 6000000,
								    "domesticStockAmount": 3000000,
								    "foreignStockAmount": 2000000,
								    "alternativeAmount": 1000000
								  }
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.financialProfile.age").value(24))
				.andExpect(jsonPath("$.result.financialProfile.monthlyIncome").value(2500000))
				.andExpect(jsonPath("$.result.financialAsset.depositBondAmount").value(6000000));

		// then: 저장된 금융 정보를 다시 조회할 수 있다.
		mockMvc.perform(get("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.financialProfile.age").value(24))
				.andExpect(jsonPath("$.result.financialAsset.foreignStockAmount").value(2000000));
	}

	@Test
	void putFinancialInfoUpdatesExistingFinancialInfo() throws Exception {
		// given: 이미 금융 정보가 저장되어 있다.
		saveFinancialInfo();

		// when: 같은 사용자가 금융 정보를 다시 저장한다.
		mockMvc.perform(put("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "financialProfile": {
								    "age": 25,
								    "monthlyIncome": 3000000,
								    "fixedExpense": 1000000,
								    "savingsGoal": 900000,
								    "totalAssetAmount": 15000000,
								    "totalDebtAmount": 2000000
								  },
								  "financialAsset": {
								    "depositBondAmount": 8000000,
								    "domesticStockAmount": 4000000,
								    "foreignStockAmount": 2500000,
								    "alternativeAmount": 500000
								  }
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isSuccess").value(true))
				.andExpect(jsonPath("$.result.financialProfile.age").value(25))
				.andExpect(jsonPath("$.result.financialProfile.monthlyIncome").value(3000000))
				.andExpect(jsonPath("$.result.financialAsset.depositBondAmount").value(8000000));

		// then: 기존 금융 정보가 새 값으로 수정된다.
		mockMvc.perform(get("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result.financialProfile.age").value(25))
				.andExpect(jsonPath("$.result.financialAsset.alternativeAmount").value(500000));
	}

	@Test
	void putFinancialInfoFailsWhenAgeIsInvalid() throws Exception {
		// when & then: 나이가 0 이하이면 요청 검증에 실패한다.
		mockMvc.perform(put("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "financialProfile": {
								    "age": 0,
								    "monthlyIncome": 2500000,
								    "fixedExpense": 900000,
								    "savingsGoal": 700000,
								    "totalAssetAmount": 12000000,
								    "totalDebtAmount": 3000000
								  },
								  "financialAsset": {
								    "depositBondAmount": 6000000,
								    "domesticStockAmount": 3000000,
								    "foreignStockAmount": 2000000,
								    "alternativeAmount": 1000000
								  }
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void putFinancialInfoFailsWhenAmountIsNegative() throws Exception {
		// when & then: 금액이 음수이면 요청 검증에 실패한다.
		mockMvc.perform(put("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "financialProfile": {
								    "age": 24,
								    "monthlyIncome": -1,
								    "fixedExpense": 900000,
								    "savingsGoal": 700000,
								    "totalAssetAmount": 12000000,
								    "totalDebtAmount": 3000000
								  },
								  "financialAsset": {
								    "depositBondAmount": 6000000,
								    "domesticStockAmount": 3000000,
								    "foreignStockAmount": 2000000,
								    "alternativeAmount": 1000000
								  }
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void putFinancialInfoFailsWhenAgeExceedsLimit() throws Exception {
		// when & then: 나이가 허용 범위를 초과하면 요청 검증에 실패한다.
		mockMvc.perform(put("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "financialProfile": {
								    "age": 121,
								    "monthlyIncome": 2500000,
								    "fixedExpense": 900000,
								    "savingsGoal": 700000,
								    "totalAssetAmount": 12000000,
								    "totalDebtAmount": 3000000
								  },
								  "financialAsset": {
								    "depositBondAmount": 6000000,
								    "domesticStockAmount": 3000000,
								    "foreignStockAmount": 2000000,
								    "alternativeAmount": 1000000
								  }
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	@Test
	void putFinancialInfoFailsWhenRequestTypeIsInvalid() throws Exception {
		// when & then: JSON 타입이 맞지 않으면 400 공통 응답을 반환한다.
		mockMvc.perform(put("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "financialProfile": {
								    "age": "twenty-four",
								    "monthlyIncome": 2500000,
								    "fixedExpense": 900000,
								    "savingsGoal": 700000,
								    "totalAssetAmount": 12000000,
								    "totalDebtAmount": 3000000
								  },
								  "financialAsset": {
								    "depositBondAmount": 6000000,
								    "domesticStockAmount": 3000000,
								    "foreignStockAmount": 2000000,
								    "alternativeAmount": 1000000
								  }
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.isSuccess").value(false))
				.andExpect(jsonPath("$.code").value("COMMON400"));
	}

	private void saveFinancialInfo() throws Exception {
		mockMvc.perform(put("/api/v1/financial-info")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "financialProfile": {
								    "age": 24,
								    "monthlyIncome": 2500000,
								    "fixedExpense": 900000,
								    "savingsGoal": 700000,
								    "totalAssetAmount": 12000000,
								    "totalDebtAmount": 3000000
								  },
								  "financialAsset": {
								    "depositBondAmount": 6000000,
								    "domesticStockAmount": 3000000,
								    "foreignStockAmount": 2000000,
								    "alternativeAmount": 1000000
								  }
								}
								"""))
				.andExpect(status().isOk());
	}
}
