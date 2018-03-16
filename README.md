# raft

## Log Replication

1. 客户端发送请求到leader
2. leader将term和nextIndex发给follower
3. follower返回接收应答
4. leader更新自身log条目，回应客户端接收到请求，发送确认更新log给follower



## Leader Election
