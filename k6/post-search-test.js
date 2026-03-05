import { group, sleep, check } from 'k6';
import http from 'k6/http';
import { signUp, login } from './modules/auth.js';

export const options = {
  stages: [
    { duration: '5s', target: 50 },
    { duration: '20s', target: 100 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    'http_req_failed': ['rate<0.01'],
    'http_req_duration{endpoint:getPostListByCategory}': ['p(95)<1000'],
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  const ts = `${__VU}_${Math.floor(Math.random() * 1000000)}`;
  const user = { email: `p_test_${ts}@test.com`, pw: 'password123!', nick: `p_tester_${ts}` };
  const headers = { 'Content-Type': 'application/json' };

  signUp(BASE_URL, user.email, user.pw, user.nick, { headers });
  const token = login(BASE_URL, user.email, user.pw, { headers });

  if (token) {
    let lastPostId = ''; // 처음엔 null(빈값)로 시작
    const categoryId = Math.floor(Math.random() * 5) + 1;

    group('03. Post Domain Performance - Category Search', function () {
      // 3번 연속으로 다음 페이지를 조회하도록 시뮬레이션
      for (let i = 0; i < 3; i++) {
        const url = `${BASE_URL}/posts/list?categoryId=${categoryId}&size=20&lastPostId=${lastPostId}`;

        const res = http.get(url, {
          headers: { ...headers, 'Authorization': `Bearer ${token}` },
          tags: { endpoint: 'getPostListByCategory' }
        });

        const isOk = check(res, {
          'is status 200': (r) => r.status === 200,
          'success true': (r) => JSON.parse(r.body).success === true,
        });

        if (isOk) {
          const body = JSON.parse(res.body);
          const contents = body.data.content;
          if (contents && contents.length > 0) {
            // 마지막 아이템의 ID를 업데이트하여 다음 "더보기" 요청에 사용
            lastPostId = contents[contents.length - 1].id;
          } else {
            break; // 더 이상 데이터 없으면 중단
          }
        }
        sleep(0.05); // 연속 요청 사이의 짧은 간격
      }
    });
  }
  sleep(0.1);
}