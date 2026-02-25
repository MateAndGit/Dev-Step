import http from 'k6/http';

// 1. 댓글 생성 (부모 댓글 또는 일반 댓글)
export function createComment(baseUrl, postId, payload, params) {
  // payload 예시: { parentId: null, content: "댓글 내용" }
  return http.post(`${baseUrl}/posts/${postId}/comments`, JSON.stringify(payload), params);
}

// 2. 댓글 수정
export function updateComment(baseUrl, postId, commentId, payload, params) {
  return http.put(`${baseUrl}/posts/${postId}/comments/${commentId}`, JSON.stringify(payload), params);
}

// 3. 댓글 삭제
export function deleteComment(baseUrl, postId, commentId, params) {
  return http.del(`${baseUrl}/posts/${postId}/comments/${commentId}`, null, params);
}