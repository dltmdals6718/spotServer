# API 명세서

<hr>

### API 요청 헤더

유저 정보가 필요 없는, 서버에서 인증 과정을 필요로하지 않는 요청은 Authorization 헤더가 필요 없지만<br>
POST, PUT, DELETE와 같은 인증이 필요한 요청은 Authorization 헤더를 필요로 합니다.

<br>

<table>
    <thead>
        <th>요청 헤더명</th>
        <th>설명</th>
    </thead>
    <tbody>
        <tr>
            <td>Authorization</td>
            <td>인증을 필요로하는 요청을 하기 위해 접근 토큰(access_token)을 전달하는 헤더. <br> Authorization : {토큰 타입} {토큰 값}</td>
        </tr>
    </tbody>
</table>

#### 요청 헤더 예

Authorization : Bearer AaA.bBb.CcC


<hr>

### 에러 메시지 형식

에러 메시지의 형식은 JSON이며 다음과 같이 코드와 메시지를 갖는다.

```json
{
  "errorCode": "NOT_VALID",
  "message": "아이디를 비울 수 없습니다."
}
```

해당 에러 코드에 대한 상세한 내용은 message로 제공되며, 응답의 HTTP 상태 코드 또한 포함됩니다.

<br>

#### 공통 에러 코드

<table>
    <thead>
        <tr>
            <th>HTTP 상태 코드</th>
            <th>에러 코드</th>
            <th>설명</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>400 Bad Request</td>
            <td>"NOT_VALID"</td>
            <td>API 요청시 필요한 필수 정보가 없습니다.</td>
        </tr>
        <tr>
            <td>401 Unauthorized</td>
            <td>"UNAUTHORIZED_CLIENT"</td>
            <td>토큰 정보가 없습니다.</td>
        </tr>
        <tr>
            <td>401 Unauthorized</td>
            <td>"EXPIRED_TOKEN"</td>
            <td>만료된 토큰입니다.</td>
        </tr>
        <tr>
            <td>401 Unauthorized</td>
            <td>"JWT_DECODE_FAIL"</td>
            <td>토큰 정보가 올바르지 않습니다.</td>
        </tr>
        <tr>
            <td>401 Unauthorized</td>
            <td>"JWT_SIGNATURE_FAIL"</td>
            <td>토큰 정보가 올바르지 않습니다.</td>
        </tr>
        <tr>
            <td>403 Forbidden</td>
            <td>"FORBIDDEN_CLIENT"</td>
            <td>접근 권한이 없습니다.</td>
        </tr>
        <tr>
            <td>404 Not Found</td>
            <td>"NO_SUCH_ELEMENT"</td>
            <td>요청한 데이터가 존재하지 않습니다.</td>
        </tr>
        <tr>
            <td>415 Unsupported Media Type</td>
            <td>"NOT_SUPPORTED_CONTENT_TYPE"</td>
            <td>요청의 Content-Type이 올바르지 않습니다.</td>
        </tr>
    </tbody>
</table>

<hr>

#### 공통 응답 타입

PageInfo
<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>page</td>
            <td>int</td>
            <td>페이지 번호</td>
        </tr>
        <tr>
            <td>size</td>
            <td>int</td>
            <td>페이지 크기</td>
        </tr>
        <tr>
            <td>numberOfElements</td>
            <td>int</td>
            <td>현재 페이지의 요소 개수</td>
        </tr>
        <tr>
            <td>totalElements</td>
            <td>Long</td>
            <td>전체 요소 개수</td>
        </tr>
        <tr>
            <td>totalPage</td>
            <td>int</td>
            <td>전체 페이지 개수</td>
        </tr>
    </tbody>
</table>



<hr>

### MEMBER

<table>
    <thead>
        <tr>
            <td>Method</td>
            <td>URL</td>
            <td>Request Body</td>
            <td>Response Body</td>
            <td>Description</td>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>POST</td>
            <td>/mails/certification<br>?mail={이메일 주소}</td>
            <td></td>
            <td></td>
            <td>이메일로 인증 번호 전송</td>
        </tr>
  <tr>
    <td>POST</td>
    <td>/members/signup</td>
  <td>
Content-Type : multipart/form-data <br>

이름: signUpMember <br>
설명: 회원 가입 정보

```json
{
  "name": "닉네임",
  "loginId": "아이디",
  "loginPwd": "비밀번호",
  "mail" : "test@abc.com",
  "code" : 1234
}
```

<br>
이름: memberImg <br>
설명: 프로필 이미지 <br>
필수 : X
  </td>
<td>

```json
{
  "memberId": 150,
  "name": "닉네임",
  "role": "USER",
  "memberImg": "http://localhost:8080/members/150/images/805a1742-6e12-4dac-81c3-c2e424e91ac3.png"
}
```

</td>
    <td>회원가입</td>
  </tr>

  <tr>
    <td>POST</td>
    <td>/members/signin</td>
<td>

```json
{
  "loginId": "아이디",
  "loginPwd": "비밀번호"
}
```

</td>
<td>

```json
{
  "expire_in": 1500,
  "token": "tokenString"
}
```

</td>
    <td>로그인</td>
  </tr>

<tr>
    <td>POST</td>
    <td>/members/signin-kakao<br>?kakaoToken={kakaoSDK 로그인을 통해 받은 액세스 토큰}</td>
    <td></td>
<td>

```json
{
  "expire_in": 1500,
  "token": "tokenString"
}
```

</td>
    <td>카카오 로그인</td>
<tr>


<tr>
    <td>PUT</td>
    <td>/members/{memberId}</td>
<td>

Content-Type : multipart/form-data <br>

이름: memberUpdateRequest <br>
설명: 하나의 필드라도 변경을 원한다면, 변경하지 않을 회원 필드도 현재 값으로 넣어서 요청 필요.

```json
{
  "name": "a b"
}
```

<br>
이름: memberImg <br>
설명: 변경할 프로필 이미지 <br>
필수: X
</td>
<td>

```json
{
  "memberId": 189,
  "name": "a b",
  "role": "USER",
  "memberImg": "http://localhost:8080/members/189/images/exmaple.jpeg"
}
```

</td>
    <td>회원 정보 수정</td>
</tr>

<tr>
    <td>GET</td>
    <td>/members/{memberId}</td>
    <td></td>
<td>

```json
{
  "memberId": 150,
  "name": "닉네임",
  "role": "USER",
  "memberImg": "http://localhost:8080/members/150/images/805a1742-6e12-4dac-81c3-c2e424e91ac3.png"
}
```

</td>
    <td>특정 회원 정보 조회</td>
</tr>

<tr>
    <td>GET</td>
    <td>/members</td>
    <td></td>
<td>

```json
{
  "memberId": 109,
  "name": "LSM",
  "role": "USER",
  "memberImg": "http://localhost:8080/members/109/images/d65d8d3d-19ac-4bf0-9860-d62fbdd76286.png"
}
```

</td>
    <td>자신 정보 조회</td>
</tr>
        <tr>
            <td>GET</td>
            <td>/members/like-locations<br>?page={페이지번호}</td>
            <td></td>
<td>

```json
{
    "results": [
        {
            "locationId": 62,
            "latitude": 35.24308,
            "longitude": 128.6934,
            "title": "테스트장소1",
            "address": "주소",
            "description": "설명",
            "regDate": "2024-03-18T15:17:33",
            "likeCnt": 1
        }
    ],
    "pageInfo": {
        "page": 1,
        "size": 5,
        "numberOfElements": 1,
        "totalElements": 1,
        "totalPage": 1
    }
}
```
</td>
            <td>좋아요 장소 조회</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/members/like-posters<br>?page={페이지번호}</td>
            <td></td>
<td>

```json
{
    "results": [
        {
            "posterId": 17,
            "writerId": 7,
            "writerName": "작성자 닉네임",
            "title": "게시글 제목",
            "content": "게시글 내용",
            "regDate": "2024-03-16T06:57:42",
            "likeCnt": 1,
            "commentCnt": 0
        }
    ],
    "pageInfo": {
        "page": 1,
        "size": 5,
        "numberOfElements": 1,
        "totalElements": 1,
        "totalPage": 1
    }
}
```
</td>
            <td>좋아요 게시글 조회</td>
        </tr>
    </tbody>
</table>

### 1. 요청

#### 1.1 회원가입

##### 쿼리 파라미터

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>name</td>
            <td>String</td>
            <td>닉네임</td>
            <td>O</td>
        </tr>
        <tr>
            <td>loginId</td>
            <td>String</td>
            <td>아이디</td>
            <td>O</td>
        </tr>
        <tr>
            <td>loginPwd</td>
            <td>String</td>
            <td>비밀번호</td>
            <td>O</td>
        </tr>
        <tr>
            <td>mail</td>
            <td>String</td>
            <td>이메일</td>
            <td>O</td>
        </tr>
        <tr>
            <td>code</td>
            <td>Integer</td>
            <td>이메일 인증 번호</td>
            <td>O</td>
        </tr>
    </tbody>
</table>


### 2. 응답

#### 2.1 로그인

##### 응답

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>expire_in</td>
            <td>Long</td>
            <td>토큰 만료 시간을 나타내며 단위는 초이다.</td>
        </tr>
        <tr>
            <td>token</td>
            <td>String</td>
            <td>사용자 토큰 값</td>
        </tr>
    </tbody>
</table>

<br>

### 3.  에러 코드

<table>
    <thead>
        <tr>
            <th>HTTP 상태 코드</th>
            <th>에러 코드</th>
            <th>에러 메시지</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>400</td>
            <td>"NOT_VALID"</td>
            <td>아이디, 비밀번호 또는 닉네임을 입력하지 않음</td>
        </tr>
        <tr>
            <td>401</td>
            <td>"FAIL_LOGIN"</td>
            <td>잘못된 아이디, 비밀번호를 입력 했음</td>
        </tr>
        <tr>
            <td rowspan="2">409</td>
            <td>"DUPLICATE_LOGINID"</td>
            <td rowspan="2">이미 다른 유저가 사용중인 닉네임 또는 아이디를 등록</td>
        </tr>
        <tr>
            <td>"DUPLICATE_NAME"</td>
        </tr>
    </tbody>
</table>


<hr>

### LOCATION

<table>
    <thead>
        <tr>
            <th>Method</th>
            <th>URL</th>
            <th>Request</th>
            <th>Response</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>GET</td>
            <td>/locations<br>?latitude={위도값}<br>&longitude={경도값}<br>&size={페이지크기}<br>&page={페이지번호}<br>&sort={정렬방법}<br>&search={제목 또는 내용}<br>&approve={승인여부}</td>
            <td></td>
<td>

```json

{
  "results": [
    {
      "locationId": 4,
      "latitude": 35.24154,
      "longitude": 128.6957,
      "title": "제목",
      "address": "주소",
      "description": "설명",
      "regDate": "2024-02-15T14:08:41",
      "likeCnt": 0
    },
    {
      "...": "..."
    },
    {
      "...": "..."
    },
    {
      "...": "..."
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 4,
    "numberOfElements": 4,
    "totalElements": 4,
    "totalPage": 1
  }
}

```

</td>
            <td>위도, 경도로 주위 장소 조회</td>
        </tr>
        <tr>
            <td>POST</td>
            <td>/locations</td>
<td>

Content-Type : multipart/form-data

이름 : locationRequest <br>
설명 : 장소 정보 <br>
필수 : O

```json
{
  "latitude": 1.1,
  "longitude": 2.2,
  "title": "테스트장소",
  "address": "주소",
  "description": "설명"
}
```

<br>
이름 : files <br>
설명 : 첨부 이미지 파일 <br>
필수 : X

</td>
<td>

```json
{
  "locationId": 9
}
```

</td>
            <td>장소 등록</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/locations/{locationId}</td>
            <td></td>
<td>

```json
{
  "locationId": 2,
  "latitude": 35.23296,
  "longitude": 128.6805,
  "title": "용지호수공원",
  "address": "경상남도 창원시 의창구 용지동 551-4",
  "description": "용지호수는 경상남도 창원시 성산구 용지동에 있는 호수이다. 창원시를 대표하는 호수이며, 용지공원 안에 있다.",
  "regDate": "2024-02-15T14:08:30",
  "likeCnt": 1
}
```

</td>
            <td>장소ID로 조회</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/locations/best</td>
            <td></td>
<td>

```json
[
  {
    "locationId": 8,
    "latitude": 35.24594,
    "longitude": 128.6939,
    "title": "제목",
    "address": "주소",
    "description": "설명",
    "regDate": "2024-02-15T13:40:58",
    "likeCnt": 0
  },
  {
    "...": "..."
  }
]
```

</td>
            <td>전국 좋아요 상위 5개 장소</td>
        </tr>
<tr>
    <td>PUT</td>
    <td>/locations/{locationId}/approve</td>
<td>

```json
{
  "approve": true
}
```

*approve : 승인 여부 <br>
true - 승인으로 변경 <br>
false - 미승인으로 변경
</td>
<td>

```json
{
  "locationId": 1,
  "approve": true
}
```

</td>
    <td>장소 승인 상태 변경 (어드민 권한)</td>
</tr>

<tr>
    <td>DELETE</td>
    <td>/locations/{locationId}</td>
    <td></td>
    <td>삭제 성공시 HTTP 상태 코드는 204를 가지며 responseBody는 갖지 않는다.</td>
    <td>장소 삭제(어드민 권한)</td>
</tr>


<tr>
    <td>GET</td>
    <td>/locations/{locationId}/likes</td>
    <td></td>
<td>

```json
{
  "likeCnt": 2
}
```

</td>
            <td>좋아요 개수 조회</td>
        </tr>
        <tr>
            <td>POST</td>
            <td>/locations/{locationId}/likes</td>
            <td></td>
            <td></td>
            <td>좋아요 등록</td>
        </tr>
        <tr>
            <td>DELETE</td>
            <td>/locations/{locationId}/likes</td>
            <td></td>
            <td>취소 성공시 HTTP 상태 코드는 204를 가지며 responseBody는 갖지 않는다.</td>
            <td>좋아요 취소</td>
        </tr>
    </tbody>
</table>

### 1. 요청

#### 1.1 주위 장소 조회

##### 쿼리 파라미터

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>latitude</td>
            <td>Double</td>
            <td>위도값</td>
            <td>O</td>
        </tr>
        <tr>
            <td>longitude</td>
            <td>Double</td>
            <td>경도값</td>
            <td>O</td>
        </tr>
        <tr>
            <td>page</td>
            <td>Integer</td>
            <td>요청할 페이지 번호<br>(기본값:1)</td>
            <td>X</td>
        </tr>
        <tr>
            <td>size</td>
            <td>Integer</td>
            <td>한 페이지에 담길 최대 데이터 개수<br>(최대:30, 기본값:10)</td>
            <td>X</td>
        </tr>
        <tr>
            <td>sort</td>
            <td>String</td>
            <td>데이터 정렬 방식으로 다음중 하나를 값으로 갖는다.<br>recent: 최신순, like: 좋아요순<br>(기본값: recent)</td>
            <td>X</td>
        </tr>
        <tr>
            <td>search</td>
            <td>String</td>
            <td>제목, 내용에 포함된 키워드를 검색</td>
            <td>X</td>
        </tr>
        <tr>
            <td>approve</td>
            <td>Boolean</td>
            <td>
                승인, 미승인 장소 구분 (미승인 장소 조회는 ADMIN만 가능)<br>
                (0: 미승인, 1: 승인, 기본값: 1)
            </td>
            <td>X</td>
        </tr>
    </tbody>
</table>
<br>
요청 예
<table>
    <thead>
        <tr>
            <th>요청 경로</th>
            <th>설명</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>/locations?latitude=위도&longitude=경도</td>
            <td>지도에 장소 표시를 위한 API이다. 한 페이지에 주위 모든 장소들을 응답으로 받습니다.</td>
        </tr>
        <tr>
            <td>위도, 경도 외 추가적인 파라미터 적용</td>
            <td>게시글 형태로 장소들을 조회하기 위한 API이다. 검색, 정렬과 같은 조건들을 넣을 수 있다.</td>
        </tr>
    </tbody>
</table>

### POSTER

<table>
  <td>Method</td>
  <td>URL</td>
  <td>Request Body</td>
  <td>Response</td>
  <td>Description</td>
  <tr>
    <td>POST</td>
    <td>/locations/{locationId}/posters</td>
  <td>

Content-Type : multipart/form-data <br>

이름 : posterRequest <br>
설명 : 게시글 내용 <br>
필수 : O

```json
{
  "title": "HAHA",
  "content": "HOHO"
}
```

<br>
이름 : files <br>
설명 : 첨부 이미지 파일 <br>
필수 : X

  </td>
<td>

```json
{
  "posterId": 66
}
```

</td>
    <td>게시글 작성</td>
  </tr>

  <tr>
    <td>GET</td>
    <td>/locations/{locationId}/posters<br>?page={페이지번호}<br>&size={페이지크기}<br>&sort={정렬방법}</td>
<td>
</td>
<td>

```json
{
  "results": [
    {
      "posterId": 61,
      "writerId": 129,
      "writerName": "testName",
      "title": "새글",
      "content": "content",
      "regDate": "2024-02-06T15:40:47",
      "likeCnt": 1,
      "commentCnt": 2
    },
    {
      "posterId": 60,
      "writerId": 129,
      "writerName": "testName",
      "title": "title",
      "content": "content",
      "regDate": "2024-02-05T16:49:47",
      "likeCnt": 1,
      "commentCnt": 0
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 2,
    "numberOfElements": 2,
    "totalElements": 4,
    "totalPage": 2
  }
}
```

</td>
    <td>전체 게시글 조회</td>
  </tr>

<tr>
    <td>GET</td>
    <td>/posters/{posterId}</td>
<td>
</td>
<td>

```json
{
  "posterId": 61,
  "writerId": 129,
  "writerName": "testName",
  "title": "새글",
  "content": "content",
  "regDate": "2024-02-06T15:40:47",
  "likeCnt": 1,
  "commentCnt": 2
}
```

</td>
    <td>특정 게시글 조회</td>
  </tr>

<tr>
    <td>GET</td>
    <td>/posters/best</td>
    <td></td>
<td>

```json
[
  {
    "posterId": 61,
    "writerId": 129,
    "writerName": "testName",
    "title": "새글",
    "content": "content",
    "regDate": "2024-02-06T15:40:47",
    "likeCnt": 1,
    "commentCnt": 2
  },
  {
    "...": "..."
  }
]
```

</td>
    <td>좋아요 상위 5개 포스터</td>
</tr>

<tr>
    <td>PUT</td>
    <td>/posters/{posterId}</td>
<td>
Content-Type : multipart/form-data <br>
이름 : posterRequest <br>
설명 : 수정할 게시글 내용 <br>
필수 : O <br>

```json
{
  "title": "수정된 제목2",
  "content": "수정된 제목2"
}
```

<br>
이름 : addFiles <br>
설명 : 추가할 파일 <br>
필수 : X <br>


<br>
이름 : deleteFilesId <br>
설명 : 삭제할 파일 ID <br>
필수 : X <br>

```json
[
  48,
  49
]
```

</td>
<td>

```json
{
  "posterId": 66
}
```

</td>
    <td>게시글 수정</td>
</tr>

<tr>
    <td>DELETE</td>
    <td>/posters/{posterId}</td>
    <td></td>
    <td>삭제 성공시 HTTP 상태 코드는 204를 가지며 responseBody는 갖지 않는다.</td>
    <td>게시글 삭제</td>
</tr>
        <tr>
            <td>GET</td>
            <td>/posters/{posterId}/likes</td>
            <td></td>
<td>

```json
{
  "likeCnt": 1
}
```

</td>
            <td>좋아요 개수 조회</td>
        </tr>
        <tr>
            <td>POST</td>
            <td>/posters/{posterId}/likes</td>
            <td></td>
            <td></td>
            <td>좋아요 등록</td>
        </tr>
        <tr>
            <td>DELETE</td>
            <td>/posters/{posterId}/likes</td>
            <td></td>
            <td>취소 성공시 HTTP 상태 코드는 204를 가지며 responseBody는 갖지 않는다.</td>
            <td>좋아요 취소</td>
        </tr>


</table>

### 1. 요청

#### 1.1 전체 게시글 조회

##### 쿼리 파라미터

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>page</td>
            <td>int</td>
            <td>요청할 페이지 번호<br>(최소:1, 기본값:1)</td>
            <td>X</td>
        </tr>
        <tr>
            <td>size</td>
            <td>int</td>
            <td>한 페이지에 담길 최대 데이터 개수<br>(최소:1, 최대:30, 기본값:10)</td>
            <td>X</td>
        </tr>
        <tr>
            <td>sort</td>
            <td>String</td>
            <td>데이터 정렬 방식으로 다음중 하나를 값으로 갖는다.<br>recent: 최신순, like: 좋아요순<br>(기본값: recent)</td>
            <td>X</td>
        </tr>
    </tbody>
</table>

#### 1.2 게시글 작성

##### 헤더

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Authorization</td>
            <td>사용자 인증 수단으로 토큰 값을 쓴다.<br>Authorization: Bearer {tokenString} </td>
            <td>O</td>
        </tr>
        <tr>
            <td>Content-Type</td>
            <td>Multipart/form-data</td>
            <td>O</td>
        </tr>
    </tbody>
</table>

##### 본문

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>posterRequest</td>
            <td>PosterRequest(application/json)</td>
            <td>게시글 작성 정보</td>
            <td>O</td>
        </tr>
        <tr>
            <td>files</td>
            <td>File</td>
            <td>첨부 이미지 파일<br>multipart 형식으로 전송해야합니다.</td>
            <td>X</td>
        </tr>
    </tbody>
</table>

PosterRequest
<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>title</td>
            <td>String</td>
            <td>게시글 제목</td>
            <td>O</td>
        </tr>
        <tr>
            <td>content</td>
            <td>String</td>
            <td>게시글 내용</td>
            <td>O</td>
        </tr>
    </tbody>
</table>

#### 1.3 게시글 수정

##### 헤더

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Authorization</td>
            <td>사용자 인증 수단으로 토큰 값을 쓴다.<br>Authorization: Bearer {tokenString} </td>
            <td>O</td>
        </tr>
        <tr>
            <td>Content-Type</td>
            <td>Multipart/form-data</td>
            <td>O</td>
        </tr>
    </tbody>
</table>

##### 본문

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
            <th>필수</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>posterRequest</td>
            <td>PosterRequest(application/json)</td>
            <td>게시글 수정 정보</td>
            <td>O</td>
        </tr>
        <tr>
            <td>addFiles</td>
            <td>File</td>
            <td>첨부 이미지 파일<br>multipart 형식으로 전송해야합니다.</td>
            <td>X</td>
        </tr>
        <tr>
            <td>deleteFilesId</td>
            <td>List&lt;Long&gt(application/json)</td>
            <td>삭제할 파일의 ID 정보</td>
            <td>X</td>
        </tr>
    </tbody>
</table>

### 2. 응답

#### 2.1 전체 게시글 조회

<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>results</td>
            <td>List&lt;Results&gt</td>
            <td>요청한 페이징된 정보</td>
        </tr>
        <tr>
            <td>pageInfo</td>
            <td>PageInfo</td>
            <td>페이징 정보</td>
        </tr>
    </tbody>
</table>

Results
<table>
    <thead>
        <tr>
            <th>이름</th>
            <th>타입</th>
            <th>설명</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>posterId</td>
            <td>Long</td>
            <td>게시글ID 값</td>
        </tr>
        <tr>
            <td>writerId</td>
            <td>Long</td>
            <td>작성자ID 값</td>
        </tr>
        <tr>
            <td>title</td>
            <td>String</td>
            <td>제목</td>
        </tr>
        <tr>
            <td>content</td>
            <td>String</td>
            <td>내용</td>
        </tr>
        <tr>
            <td>regDate</td>
            <td>String</td>
            <td>작성일</td>
        </tr>
    </tbody>
</table>



<hr>

### COMMENT

<table>
    <thead>
        <tr>
            <th>Method</th>
            <th>URL</th>
            <th>Request</th>
            <th>Response</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>POST</td>
            <td>/posters/{posterId}/comments</td>
<td>

```json
{
  "content": "댓글내용"
}
```

</td>
<td>

```json
{
  "commentId": 36
}
```

</td>
            <td>포스터 댓글 작성</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>
            /posters/{posterId}/comments<br>?page={페이지번호}<br>&size={페이지크기}<br>&sort={정렬방법}<br><br>
            *정렬방법 <br>
            recent(기본값), like(좋아요순)
            </td>
            <td></td>
<td>

```json
{
  "results": [
    {
      "commentId": 34,
      "writerId": 191,
      "writerName": "닉네임",
      "memberImg": "http://localhost:8080/members/191/images/d9b22089-0d9a-49ca-aa8f-bd7b1f898bc6.jpeg",
      "content": "히히",
      "regDate": "2024-02-25T21:53:44",
      "likeCnt": 2
    },
    {
      "commentId": 26,
      "writerId": 129,
      "writerName": "testName",
      "memberImg": "",
      "content": "댓글내용",
      "regDate": "2024-02-09T22:42:54",
      "likeCnt": 0
    },
    {
      "commentId": 25,
      "writerId": 129,
      "writerName": "testName",
      "memberImg": "",
      "content": "댓글내용",
      "regDate": "2024-02-06T15:43:49",
      "likeCnt": 3
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 3,
    "numberOfElements": 3,
    "totalElements": 3,
    "totalPage": 1
  }
}
```

</td>
            <td>특정 포스터의 댓글 조회</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/comments/{commentId}</td>
            <td></td>
<td>

```json
{
  "commentId": 5,
  "writerId": 1,
  "writerName": "namezz",
  "content": "테스트입니다.",
  "regDate": "2024-01-25T15:51:48"
}
```

</td>
            <td>특정 댓글 조회</td>
        </tr>
        <tr>
            <td>PUT</td>
            <td>/comments/{commentId}</td>
<td>

```json
{
  "content": "수정될 내용"
}
```

</td>
<td>

```json
{
  "commentId": 36
}
```

</td>
            <td>댓글 수정</td>
        </tr>
        <tr>
            <td>DELETE</td>
            <td>/comments/{commentId}</td>
            <td></td>
            <td>삭제 성공시 HTTP 상태 코드는 204를 가지며 responseBody는 갖지 않는다.</td>
            <td>댓글 삭제</td>
        </tr>
        <tr>
            <td>POST</td>
            <td>/comments/{commentId}/likes</td>
            <td></td>
            <td></td>
            <td>좋아요 등록</td>
        </tr>
        <tr>
            <td>DELETE</td>
            <td>/comments/{commentId}/likes</td>
            <td></td>
            <td>취소 성공시 HTTP 상태 코드는 204를 가지며 responseBody는 갖지 않는다.</td>
            <td>좋아요 취소</td>
        </tr>
    </tbody>
</table>


<br>
<hr>
<br>

### IMAGEFILE

<table>
    <thead>
        <tr>
            <th>Method</th>
            <th>URL</th>
            <th>Request</th>
            <th>Response</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>GET</td>
            <td>/posters/{posterId}/images</td>
            <td></td>
<td>

```json
[
  {
    "id": 11,
    "uploadFileName": "3.png"
  },
  {
    "id": 12,
    "uploadFileName": "2.png"
  }
]
```

</td>
            <td>특정 게시글의 첨부 이미지 목록 조회</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/posters/images/{posterImageId}</td>
            <td></td>
            <td>(body에 이미지파일. 아니면 파일 자체 링크로도 좋을듯)</td>
            <td>특정 이미지 조회</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/locations/{locationId}/images</td>
            <td></td>
<td>

```json
[
  {
    "id": 1,
    "uploadFileName": "1.png"
  },
  {
    "id": 2,
    "uploadFileName": "zzhaha.png"
  }
]
```

</td>
            <td>특정 장소의 첨부 이미지 목록 조회</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/locations/images/{locationImageId}</td>
            <td></td>
            <td>(body에 이미지파일. 아니면 파일 자체 링크도 좋을듯)</td>
            <td>특정 이미지 조회</td>
        </tr>
    </tbody>
</table>
