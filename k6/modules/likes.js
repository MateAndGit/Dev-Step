import http from 'k6/http';

// 1. 좋아요 누르기
export function likePost(baseUrl, postId, params) {
  return http.post(`${baseUrl}/post-likes/${postId}`, null, params);
}

// 2. 좋아요 취소
export function cancelLikePost(baseUrl, postId, params) {
  return http.post(`${baseUrl}/post-likes/${postId}/cancel`, null, params);
}