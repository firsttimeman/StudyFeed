import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

export const options = {
    scenarios: {
        feed_mixed: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "1m", target: 20 },
                { duration: "1m", target: 50 },
                { duration: "1m", target: 100 },
                { duration: "1m", target: 100 },
                { duration: "1m", target: 0 },
            ],
            gracefulStop: "10s",
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<1500"],
    },
};

const loginFails = new Counter("login_fails");
const homeFails = new Counter("home_fails");
const likeFails = new Counter("like_fails");
const commentFails = new Counter("comment_fails");

const BASE_URL = "http://spring:8080";
const PASSWORD = "1234abcd!";

// 더미 피드 수에 맞게 조정
const MAX_FEED_ID = 200;

let token;

function login(email, password) {
    const payload = JSON.stringify({ email, password });

    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    const res = http.post(`${BASE_URL}/api/auth/signin`, payload, params);

    const ok = check(res, {
        "login status is 200": (r) => r.status === 200,
        "login has access token": (r) => {
            try {
                const body = r.json();
                return !!(body.accessToken || body.access_token || body.token);
            } catch (e) {
                return false;
            }
        },
    });

    if (!ok) {
        loginFails.add(1);
        console.error(`LOGIN FAIL user=${email} status=${res.status} body=${res.body}`);
        return null;
    }

    const body = res.json();
    return body.accessToken || body.access_token || body.token;
}

function getToken() {
    if (token) return token;

    const userNo = ((__VU - 1) % 20) + 1;
    const email = `user${userNo}@test.com`;

    token = login(email, PASSWORD);
    return token;
}

function authHeaders(accessToken, json = false) {
    const headers = {
        Authorization: `Bearer ${accessToken}`,
    };

    if (json) {
        headers["Content-Type"] = "application/json";
    }

    return { headers };
}

function randomFeedId() {
    return Math.floor(Math.random() * MAX_FEED_ID) + 1;
}

function doHome(accessToken) {
    const page = Math.floor(Math.random() * 20);
    const size = 10;

    const res = http.get(
        `${BASE_URL}/api/feed/home?page=${page}&size=${size}`,
        authHeaders(accessToken)
    );

    const ok = check(res, {
        "home status is 200": (r) => r.status === 200,
    });

    if (!ok) {
        homeFails.add(1);
        console.error(`HOME FAIL VU=${__VU} status=${res.status} body=${res.body}`);
    }
}

function doLike(accessToken) {
    const feedId = randomFeedId();

    const res = http.post(
        `${BASE_URL}/api/feed/${feedId}/like`,
        null,
        authHeaders(accessToken)
    );

    const ok = check(res, {
        "like status is 200": (r) => r.status === 200,
    });

    if (!ok) {
        likeFails.add(1);
        console.error(`LIKE FAIL VU=${__VU} feedId=${feedId} status=${res.status} body=${res.body}`);
    }
}

function doComment(accessToken) {
    const feedId = randomFeedId();

    const payload = JSON.stringify({
        feedId: feedId,
        parentCommentId: null,
        content: `k6 comment from VU ${__VU} at ${Date.now()}`,
    });

    const res = http.post(
        `${BASE_URL}/api/feed/comments`,
        payload,
        authHeaders(accessToken, true)
    );

    const ok = check(res, {
        "comment status is 200": (r) => r.status === 200,
    });

    if (!ok) {
        commentFails.add(1);
        console.error(`COMMENT FAIL VU=${__VU} feedId=${feedId} status=${res.status} body=${res.body}`);
    }
}

export default function () {
    const accessToken = getToken();
    if (!accessToken) return;

    const rand = Math.random();

    // 조회 70%, 좋아요 20%, 댓글 10%
    if (rand < 0.7) {
        doHome(accessToken);
    } else if (rand < 0.9) {
        doLike(accessToken);
    } else {
        doComment(accessToken);
    }

    sleep(Math.random() * 0.5 + 0.1);
}