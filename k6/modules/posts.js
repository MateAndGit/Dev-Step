import http from 'k6/http';

// 1. 게시글 생성 (POST /create)
export function createPost(baseUrl, payload, params) {
  return http.post(`${baseUrl}/posts/create`, JSON.stringify(payload), params);
}

// 2. 게시글 목록 조회 (GET /list)
export function getPosts(baseUrl, condition, params) {
  const query = Object.keys(condition)
    .filter(key => condition[key] !== null && condition[key] !== undefined)
    .map(key => `${key}=${encodeURIComponent(condition[key])}`)
    .join('&');

  const url = `${baseUrl}/posts/list?${query}&page=0&size=10`;
  return http.get(url, params);
}

// 3. 게시글 단건 상세 조회 (GET /{postId})
export function getPostDetail(baseUrl, postId, params) {
  return http.get(`${baseUrl}/posts/${postId}`, params);
}

// 4. 게시글 수정 (PUT /{postId})
export function updatePost(baseUrl, postId, payload, params) {
  return http.put(`${baseUrl}/posts/${postId}`, JSON.stringify(payload), params);
}

// 5. 게시글 삭제 (DELETE /{postId})
export function deletePost(baseUrl, postId, params) {
  return http.del(`${baseUrl}/posts/${postId}`, null, params);
}