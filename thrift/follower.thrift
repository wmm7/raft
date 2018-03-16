namespace java thrift

struct Response{
    1:required i16 errNo
    2:required string errMsg
}

struct RequestInfo {
    1:required i16 term
    2:required i16 nextIndex
}

service Follower {

    Response leaderRequest(1:RequestInfo requestInfo);

    Response leaderCommit(1:RequestInfo requestInfo);
}
