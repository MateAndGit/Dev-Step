import { group, sleep, check } from 'k6';
import { signUp, login } from './modules/auth.js';
import { createPost, getPosts } from './modules/posts.js';
import { createComment } from './modules/comments.js';
import { likePost, cancelLikePost } from './modules/likes.js';

export const options = {
  stages: [
    { duration: '10s', target: 20 }, // 20명까지 접속자 증가
    { duration: '30s', target: 40 }, // 40명 유지 (본격 부하 테스트)
    { duration: '10s', target: 0 },  // 종료
  ],
  thresholds: {
    'http_req_failed': ['rate<0.01'], // 에러율 1% 미만
    'http_req_duration': ['p(95)<500'], // 전체 응답 0.5초 이내
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  const ts = `${__VU}_${new Date().getTime().toString().slice(-6)}`;
  const user = { email: `final_${ts}@test.com`, pw: 'password123!', nick: `user_${ts}` };
  const headers = { 'Content-Type': 'application/json' };

  // 1. 회원가입 & 로그인
  signUp(BASE_URL, user.email, user.pw, user.nick, { headers });
  const token = login(BASE_URL, user.email, user.pw, { headers });

  if (token) {
    const authParams = { headers: { ...headers, 'Authorization': `Bearer ${token}` } };

    group('Comprehensive User Journey', function () {
      // 2. 메인 피드(게시글 목록) 구경
      getPosts(BASE_URL, {}, authParams);

      // 3. 내 글 작성
      const postRes = createPost(BASE_URL, {
        title: `Integrated Test Post ${ts}`,
        content: 'This is the final performance check.'
      }, authParams);
      const postId = postRes.json().data;

      if (postId) {
        // 4. 좋아요 클릭 (중복 방지 로직 가동)
        likePost(BASE_URL, postId, authParams);

        // 5. 댓글 작성
        createComment(BASE_URL, postId, {
          parentId: null,
          content: 'LGTM! (Looks Good To Me)'
        }, authParams);

        // 6. 좋아요 취소 (상태 원복)
        cancelLikePost(BASE_URL, postId, authParams);
      }
    });
  }

  // 실제 유저의 행동 사이 대기 시간 (생략 가능하지만 1초 권장)
  sleep(1);
}