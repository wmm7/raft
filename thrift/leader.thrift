namespace java thrift

struct Result{
    1:required i16 errNo
    2:required string errMsg
}

service Leader {

    Result clientRequest();
}