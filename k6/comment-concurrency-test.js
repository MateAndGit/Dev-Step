import { group, sleep, check } from 'k6';
import { signUp, login } from './modules/auth.js';
import { createComment } from './modules/comments.js';

export const options = {
  stages: [
    { duration: '5s', target: 50 },  // 50명까지 빠르게 램프업
    { duration: '20s', target: 100 }, // 100명이 동시에 락을 획득하려고 경쟁
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    'http_req_failed': ['rate<0.05'], // 부하가 높으니 에러율 5%까지 허용해봅시다
    'http_req_duration{endpoint:bulkInsertComment}': ['p(95)<2000'], // 락 대기 고려 2s
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  // VU마다 고유한 유저 정보를 생성 (유저를 못 찾는 문제 해결)
  const ts = `${__VU}_${Math.floor(Math.random() * 1000000)}`;
  const user = {
    email: `c_test_${ts}@test.com`,
    pw: 'password123!',
    nick: `c_tester_${ts}`
  };
  const headers = { 'Content-Type': 'application/json' };

  // 1. [필수] 유저 생성 및 로그인 (댓글 권한 획득용)
  // 유저 정보 수정/삭제 같은 군더더기는 뺐습니다.
  signUp(BASE_URL, user.email, user.pw, user.nick, { headers });
  const token = login(BASE_URL, user.email, user.pw, { headers });

  if (token) {
    const authParams = { headers: { ...headers, 'Authorization': `Bearer ${token}` } };
    const TARGET_POST_ID = 1;

    group('Comment_Concurrency_Stress', function () {
      // 2. 동시성 이슈를 유발하는 댓글 작성 (이게 메인!)
      const commentRes = createComment(BASE_URL, TARGET_POST_ID,
        { parentId: null, content: `Race Condition Test - VU ${__VU}` },
        { ...authParams, tags: { endpoint: 'bulkInsertComment' } }
      );

      check(commentRes, {
        'Comment Success': (r) => r.status === 200 || r.status === 201
      });
    });
  }

  // 요청 간격을 좁혀서 충돌 확률을 최대화
  sleep(0.05);
}