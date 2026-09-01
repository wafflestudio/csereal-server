package com.wafflestudio.csereal.common.interceptor

/**
 * 컨테이너 내부(loopback) 호출만 허용하는 엔드포인트에 붙인다.
 * 인증 없는 관리용 경로를 운영자만 쓰게 하는 장치 — `docker exec <backend> curl localhost:8080/…`.
 * 학외에서는 OAuth 로그인이 안 되므로 세션은 쓰지 않는다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class InternalOnly
