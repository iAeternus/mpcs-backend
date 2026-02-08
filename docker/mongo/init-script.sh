#!/bin/sh
set -e

echo "⏳ 等待 MongoDB 可连接..."

until mongosh --host mongodb1:27017 --eval "db.adminCommand('ping')" --quiet >/dev/null 2>&1
do
  echo "  Mongo 未就绪..."
  sleep 3
done

echo "✅ Mongo 已连接"

mongosh --host mongodb1:27017 <<'EOF'

print("📦 初始化副本集");

cfg = {
  _id: "app",
  members: [
    { _id: 0, host: "mongodb1:27017" },
    { _id: 1, host: "mongodb2:27017" },
    { _id: 2, host: "mongodb3:27017" }
  ]
};

try {
  rs.status();
  print("副本集已存在");
} catch (e) {
  print("执行 rs.initiate()");
  rs.initiate(cfg);
}

function waitPrimary() {
  while (true) {
    try {
      let s = rs.status();
      for (m of s.members) {
        if (m.stateStr === "PRIMARY") {
          print("PRIMARY 就绪: " + m.name);
          return;
        }
      }
    } catch(e) {}
    print("等待 PRIMARY...");
    sleep(2000);
  }
}

waitPrimary();

print("👤 创建 admin 用户");

db = db.getSiblingDB("admin");

try {
  db.createUser({
    user: "admin",
    pwd: "123456",
    roles: [
      { role: "root", db: "admin" },
      { role: "readWrite", db: "mpcs-dev" }
    ]
  });
  print("admin 创建成功");
} catch (e) {
  print("admin 已存在");
}

db = db.getSiblingDB("mpcs-dev");

try {
  db.createCollection("init_collection");
  db.init_collection.insertOne({ initialized: true, at: new Date() });
  print("mpcs-dev 初始化完成");
} catch (e) {
  print("mpcs-dev 已存在");
}

print("🎉 初始化完成");

EOF
