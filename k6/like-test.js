import { group, sleep, check } from 'k6';
import { signUp, login } from './modules/auth.js';
import { createPost } from './modules/posts.js';
import { likePost, cancelLikePost } from './modules/likes.js';

export const options = {
  stages: [
    { duration: '5s', target: 20 },
    { duration: '20s', target: 50 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    // 좋아요 API가 0.3초 안에 응답하는지 검사
    'http_req_duration{endpoint:likePost}': ['p(95)<300'],
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  const ts = `${__VU}_${new Date().getTime().toString().slice(-6)}`;
  const user = { email: `like${ts}@test.com`, pw: 'password123!', nick: `L_${ts}` };
  const headers = { 'Content-Type': 'application/json' };

  // 1. 유저 가입 및 로그인 (좋아요를 누를 주체 생성)
  signUp(BASE_URL, user.email, user.pw, user.nick, { headers });
  const token = login(BASE_URL, user.email, user.pw, { headers });

  if (token) {
    const authParams = { headers: { ...headers, 'Authorization': `Bearer ${token}` } };

    // 2. 게시글 생성 (이 유저가 좋아요를 누를 대상)
    const postRes = createPost(BASE_URL, { title: '좋아요 테스트', content: '본문' }, authParams);
    const postId = postRes.json().data;

    if (postId) {
      group('좋아요 무한 루프 테스트', function () {

        // [A] 좋아요 누르기
        const likeRes = likePost(BASE_URL, postId, {
          ...authParams, tags: { endpoint: 'likePost' }
        });

        // 서버 로직(중복 방지) 때문에 이미 눌렀으면 400이 올 수 있지만,
        // 한 루프에 취소를 넣었으므로 여기선 200이 와야 정상입니다.
        check(likeRes, {
          '좋아요 성공(200)': (r) => r.status === 200
        });

        // 실제 유저처럼 아주 잠깐(0.1초) 쉬고 취소
        sleep(0.1);

        // [B] 좋아요 취소 (이걸 해야 다음 루프에서 또 누를 수 있음!)
        const cancelRes = cancelLikePost(BASE_URL, postId, {
          ...authParams, tags: { endpoint: 'cancelLike' }
        });

        check(cancelRes, {
          '좋아요 취소 성공(200)': (r) => r.status === 200
        });
      });
    }
  }

  // 다음 루프 돌기 전 1초 대기
  sleep(1);
}