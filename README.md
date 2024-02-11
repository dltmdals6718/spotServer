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
  <td>Method</td>
  <td>URL</td>
  <td>Request Body</td>
  <td>Response Body</td>
  <td>Description</td>
  <tr>
    <td>POST</td>
    <td>/members/signup</td>
  <td>

```json
{
  "name": "닉네임",
  "loginId": "아이디",
  "loginPwd": "비밀번호"
}
```

  </td>
<td>

```json
{
  "memberId": 4,
  "name": "TESTNAME",
  "role": "USER"
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
    <td>GET</td>
    <td>/members/{memberId}</td>
    <td></td>
<td>

```json
{
    "memberId": 1,
    "name": "TESTNAME",
    "role": "USER"
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
    "memberId": 126,
    "name": "namezz",
    "role": "USER"
}
```
</td>
    <td>자신 정보 조회</td>
</tr>

</table>

#### - 로그인

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

#### 에러 코드

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
            <td>/locations<br>?latitude={위도값}<br>&longitude={경도값}</td>
            <td></td>
<td>

```json

[
  {
    "locationId": 1,
    "latitude": 11.24308,
    "longitude": 11.6934,
    "title": "AAA",
    "address": "BBB",
    "description": "낭만 가득 운동장"
  },
  {
    "locationId": 3,
    "latitude": 11.24666,
    "longitude": 11.6909,
    "title": "CCC",
    "address": "DDD",
    "description": "공부하자 아냐 그건 너무 교과서야"
  }
]

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
  "locationId": 13,
  "title": "title4",
  "content": "content4",
  "regDate": "2024-01-24T15:13:17.118866"
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
  "locationId": 1,
  "latitude": 1.1,
  "longitude": 2.2,
  "title": "장소명",
  "address": "주소",
  "description": "부가 설명"
}
```

</td>
            <td>장소ID로 조회</td>
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


<br>
<hr>
<br>

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
  "posterId": 9,
  "writerId": 1,
  "title": "title3",
  "content": "content3",
  "regDate": "2024-01-24T14:20:48.901509"
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
            "posterId": 60,
            "writerId": 129,
            "title": "title",
            "content": "content",
            "regDate": "2024-02-05T16:49:47",
            "likeCnt": 1,
            "commentCnt": 0
        },
        {
            "posterId": 61,
            "writerId": 129,
            "title": "새글",
            "content": "content",
            "regDate": "2024-02-06T15:40:47",
            "likeCnt": 1,
            "commentCnt": 1
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


이름 : deleteFilesId <br>
설명 : 삭제할 파일 ID <br>
필수 : X <br>

```json
[48, 49]
```

</td>
<td>

```json
{
  "posterId": 49,
  "writerId": 129,
  "title": "수정된 제목2",
  "content": "수정된 제목2",
  "regDate": "2024-02-03T17:13:38"
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
            <td>/comments/{posterId}</td>
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
  "commentId": 5,
  "writerId": 1,
  "writerName": "namezz",
  "content": "댓글내용",
  "regDate": "2024-01-25T15:51:48.44347"
}
```

</td>
            <td>포스터 댓글 작성</td>
        </tr>
        <tr>
            <td>GET</td>
            <td>/posters/{posterId}/comments<br>?page={페이지번호}<br>&size={페이지크기}<br>&sort={정렬방법}</td>
            <td></td>
<td>

```json
{
  "results": [
    {
      "commentId": 8,
      "writerId": 126,
      "writerName": "namezz",
      "content": "31글의 댓글",
      "regDate": "2024-01-29T13:22:20"
    },
    {
      "commentId": 7,
      "writerId": 126,
      "writerName": "namezz",
      "content": "31글의 댓글",
      "regDate": "2024-01-29T13:22:19"
    },
    {
      "commentId": 6,
      "writerId": 126,
      "writerName": "namezz",
      "content": "31글의 댓글",
      "regDate": "2024-01-29T13:22:18"
    }
  ],
  "pageInfo": {
    "page": 4,
    "size": 3,
    "numberOfElements": 3,
    "totalElements": 12,
    "totalPage": 4
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
  "content":"수정될 내용"
}
```

</td>
<td>

```json
{
    "commentId": 21,
    "writerId": 126,
    "writerName": "namezz",
    "content": "수정될 내용",
    "regDate": "2024-02-01T14:05:03"
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
