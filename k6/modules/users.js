import http from 'k6/http';

// 1. 내 정보 조회 (GET /me)
export function getMyInfo(baseUrl, params) {
  return http.get(`${baseUrl}/users/me`, params);
}

// 2. 특정 유저 조회 (GET /{userId})
export function getUser(baseUrl, userId, params) {
  return http.get(`${baseUrl}/users/${userId}`, params);
}

// 3. 유저 정보 수정 (PUT /{userId}) - 서버가 @PutMapping 임
export function updateUser(baseUrl, userId, payload, params) {
  return http.put(`${baseUrl}/users/${userId}`, JSON.stringify(payload), params);
}

// 4. 유저 검색 (GET /search) - 서버 메서드명 searchUser 준수
export function searchUsers(baseUrl, condition, params) {
  // 페이징 파라미터 기본값 추가 (page=0&size=20)
  const query = Object.keys(condition)
    .map(key => `${key}=${encodeURIComponent(condition[key])}`)
    .join('&');

  const url = `${baseUrl}/users/search?${query}&page=0&size=20`;
  return http.get(url, params);
}

// 5. 회원 탈퇴 (DELETE /{userId})
export function deleteUser(baseUrl, userId, params) {
  return http.del(`${baseUrl}/users/${userId}`, null, params);
}