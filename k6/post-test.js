import { group, sleep, check } from 'k6';
import { signUp, login } from './modules/auth.js';
import { createPost, getPostDetail, updatePost, getPosts, deletePost } from './modules/posts.js';

export const options = {
  stages: [
    { duration: '5s', target: 5 },
    { duration: '10s', target: 10 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    'http_req_duration{endpoint:createPost}': ['p(95)<600'],
    'http_req_duration{endpoint:getPostList}': ['p(95)<400'],
    'http_req_duration{endpoint:getPostDetail}': ['p(95)<300'],
    'http_req_failed': ['rate<0.01'],
  },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
  const ts = `${__VU}_${new Date().getTime().toString().slice(-6)}`;
  const user = { email: `p${ts}@test.com`, pw: 'password123!', nick: `p_nick${ts}` };
  const headers = { 'Content-Type': 'application/json' };

  // [준비] 계정 생성 및 로그인
  signUp(BASE_URL, user.email, user.pw, user.nick, { headers });
  const token = login(BASE_URL, user.email, user.pw, { headers });

  if (token) {
    const authParams = {
      headers: { ...headers, 'Authorization': `Bearer ${token}` }
    };
    let postId = null;

    group('03. Post Domain Performance', function () {
      // 1. 게시글 생성
      const createRes = createPost(BASE_URL,
        { title: `Title ${ts}`, content: `Content ${ts}` },
        { ...authParams, tags: { endpoint: 'createPost' } }
      );

      if (check(createRes, { 'post created (200)': (r) => r.status === 200 })) {
        postId = createRes.json().data;
      }

      if (postId) {
        // 2. 단건 조회
        getPostDetail(BASE_URL, postId, {
          ...authParams, tags: { endpoint: 'getPostDetail' }
        });

        // 3. 수정
        updatePost(BASE_URL, postId,
          { title: `Updated ${ts}`, content: `Updated content` },
          { ...authParams, tags: { endpoint: 'updatePost' } }
        );

        // 4. 목록 조회
        getPosts(BASE_URL, { title: 'Updated' }, {
          ...authParams, tags: { endpoint: 'getPostList' }
        });

        // 5. 삭제 (Cleanup)
        deletePost(BASE_URL, postId, {
          ...authParams, tags: { endpoint: 'deletePost' }
        });
      }
    });
  }

  sleep(1);
}