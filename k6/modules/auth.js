import http from 'k6/http';

/**
 * 1. 회원가입 (서버 대문자 signUp 준수)
 */
export function signUp(baseUrl, email, password, nickname, params) {
  const url = `${baseUrl}/auth/signUp`;
  const payload = JSON.stringify({ email, password, nickname });
  return http.post(url, payload, params);
}

/**
 * 2. 로그인 (AccessToken 반환)
 */
export function login(baseUrl, email, password, params) {
  const url = `${baseUrl}/auth/login`;
  const payload = JSON.stringify({ email, password });
  const res = http.post(url, payload, params);

  // 서버 ApiResponse<String> 구조에서 data(accessToken) 추출
  if (res.status === 200 && res.json().data) {
    return res.json().data;
  }
  return null;
}

/**
 * 3. 토큰 재발급 (쿠키 기반)
 */
export function reissueToken(baseUrl, params) {
  const url = `${baseUrl}/auth/reissue`;
  // 서버가 @CookieValue를 사용하므로 params에 쿠키가 포함되어야 함
  return http.post(url, null, params);
}

/**
 * 4. 로그아웃
 */
export function logout(baseUrl, params) {
  const url = `${baseUrl}/auth/logout`;
  return http.post(url, null, params);
}