import { group, sleep, check } from 'k6';
import { signUp, login } from './modules/auth.js';
import { createPost } from './modules/posts.js';
import { createComment, updateComment, deleteComment } from './modules/comments.js';

export const options = {
  stages: [
    { duration: '5s', target: 5 },
    { duration: '10s', target: 10 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    'http_req_duration{endpoint:createComment}': ['p(95)<500'],
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  const ts = `${__VU}_${new Date().getTime().toString().slice(-6)}`;
  const user = { email: `c${ts}@test.com`, pw: 'password123!', nick: `c_nick${ts}` };
  const headers = { 'Content-Type': 'application/json' };

  // 1. 로그인 세션 준비
  signUp(BASE_URL, user.email, user.pw, user.nick, { headers });
  const token = login(BASE_URL, user.email, user.pw, { headers });

  if (token) {
    const authParams = { headers: { ...headers, 'Authorization': `Bearer ${token}` } };

    // 2. 게시글 먼저 생성 (댓글을 달기 위해)
    const postRes = createPost(BASE_URL, { title: `Post for Comm`, content: `Content` }, authParams);
    const postId = postRes.json().data;

    if (postId) {
      group('04. Comment Domain Performance', function () {

        // 3. 일반 댓글 작성
        const commentRes = createComment(BASE_URL, postId,
          { parentId: null, content: "This is a root comment" },
          { ...authParams, tags: { endpoint: 'createComment' } }
        );
        const commentId = commentRes.json().data;

        if (check(commentRes, { 'comment created': (r) => r.status === 200 })) {

          // 4. 대댓글(답글) 작성 (방금 만든 댓글의 ID를 parentId로 사용)
          createComment(BASE_URL, postId,
            { parentId: commentId, content: "This is a reply" },
            { ...authParams, tags: { endpoint: 'createReply' } }
          );

          // 5. 댓글 수정
          updateComment(BASE_URL, postId, commentId,
            { content: "Updated comment content" },
            { ...authParams, tags: { endpoint: 'updateComment' } }
          );

          // 6. 댓글 삭제
          deleteComment(BASE_URL, postId, commentId,
            { ...authParams, tags: { endpoint: 'deleteComment' } }
          );
        }
      });
    }
  }

  sleep(1);
}