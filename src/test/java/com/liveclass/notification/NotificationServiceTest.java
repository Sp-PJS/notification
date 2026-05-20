package com.liveclass.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import com.liveclass.notification.application.notification.NotificationService;
import com.liveclass.notification.application.notification.port.NotificationRepositoryPort;
import com.liveclass.notification.domain.notification.Notification;
import com.liveclass.notification.domain.notification.NotificationChannel;
import com.liveclass.notification.domain.notification.NotificationStatus;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepositoryPort notificationRepositoryPort;

	@InjectMocks
	private NotificationService notificationService; // 진짜 서비스 객체 주입

	@Test
	@DisplayName("handleRecoveryFailure 호출 시 retryCount가 4가 되면 상태가 FAILED_PERMANENT(최종 실패)로 직접 변경")
	void handle_recovery_failure_real_state_transition_test() {
		log.info("========================================================================================");
		log.info("[TEST START] handle_recovery_failure_real_state_transition_test");
		log.info("========================================================================================");

		// given
		UUID notificationId = UUID.randomUUID();
		Notification notification = Notification.builder()
			.userId(UUID.randomUUID())
			.channel(NotificationChannel.IN_APP)
			.eventKey("REAL_STATE_TEST_KEY")
			.content("진짜 도메인 상태 변경 테스트")
			.build();

		// 3회 실패 누적 상태 시뮬레이션
		notification.fail("1차");
		notification.fail("2차");
		notification.fail("3차"); // retryCount = 3

		log.info("[GIVEN] 진짜 엔티티 비즈니스 상태 조립 완료");
		log.info(" -> 초기화 상태: {}", notification.getStatus());
		log.info(" -> 초기화 카운트: {}", notification.getRetryCount());

		// 리포지토리가 이 진짜 엔티티를 반환하도록 Mocking
		when(notificationRepositoryPort.findById(notificationId)).thenReturn(Optional.of(notification));
		log.info("[GIVEN] 리포지토리 findById 조회 결과 반환 설정 완료");

		// when
		// 진짜 서비스의 handleRecoveryFailure 로직을 실행시킴 (4번째 실패 유도)
		log.info("\n[WHEN] 진짜 비즈니스 로직 notificationService.handleRecoveryFailure 실행 시작");
		notificationService.handleRecoveryFailure(notificationId, "4차 최종 에러 발생", 4);
		log.info("[WHEN] handleRecoveryFailure 처리 완료 (엔티티 내부 필드 갱신 확인)");

		// then
		log.info("\n[THEN] AssertJ 도메인 실 데이터 최종 정밀 검증 시작");
		log.info(" -> 실제 변경된 카운트 결과 (Actual RetryCount): {}", notification.getRetryCount());
		log.info(" -> 실제 변경된 알림 상태 결과 (Actual Status): {}", notification.getStatus());
		log.info(" -> 실제 기입된 마지막 에러 로그 (Actual ErrorMessage): {}", notification.getLastErrorMessage());

		assertThat(notification.getRetryCount()).isEqualTo(4);
		assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED_PERMANENT);
		assertThat(notification.getLastErrorMessage()).contains("4차 최종 에러 발생");
		log.info("[THEN] 성공 완료 (최종 격리방 FAILED_PERMANENT로 완전 이동 확인)");
		log.info("========================================================================================");
	}
}