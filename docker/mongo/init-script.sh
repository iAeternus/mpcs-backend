#!/bin/sh
set -e

echo "等待 MongoDB 可连接..."

until mongosh --host mongodb1:27017 --eval "db.adminCommand('ping')" --quiet >/dev/null 2>&1
do
  echo "  Mongo 未就绪..."
  sleep 3
done

echo "Mongo 已连接"

mongosh --host mongodb1:27017 <<'EOF'

print("检查副本集状态...");

try {
  rs.status();
  print("副本集已存在，跳过初始化");
} catch (e) {
  print("初始化副本集...");
  
  cfg = {
    _id: "app",
    members: [
      { _id: 0, host: "mongodb1:27017" },
      { _id: 1, host: "mongodb2:27017" },
      { _id: 2, host: "mongodb3:27017" }
    ],
    settings: {
      electionTimeoutMillis: 5000,
      heartbeatTimeoutSecs: 5
    }
  };
  
  rs.initiate(cfg);
  print("rs.initiate() 执行完成");
}

print("等待副本集配置生效...");
sleep(3000);

print("检查成员状态...");
let attempts = 0;
while (attempts < 30) {
  try {
    let s = rs.status();
    print("成员数量: " + s.members.length);
    
    for (m of s.members) {
      print("  " + m.name + ": " + m.stateStr);
    }
    
    for (m of s.members) {
      if (m.stateStr === "PRIMARY") {
        print("PRIMARY 就绪: " + m.name);
        
        print("等待 PRIMARY 完全可用...");
        sleep(5000);
        
        db = db.getSiblingDB("admin");
        print("创建 admin 用户");
        
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
          if (e.code === 5107 || e.message.includes("already exists")) {
            print("admin 已存在");
          } else {
            print("创建 admin 失败: " + e.message);
          }
        }

        db = db.getSiblingDB("mpcs-dev");

        try {
          db.createCollection("init_collection");
          db.init_collection.insertOne({ initialized: true, at: new Date() });
          print("mpcs-dev 初始化完成");
        } catch (e) {
          if (e.message.includes("already exists")) {
            print("mpcs-dev 已存在");
          } else {
            print("mpcs-dev 初始化失败: " + e.message);
          }
        }

        print("初始化完成");
        exit(0);
      }
    }
  } catch(e) {
    print("检查状态异常: " + e.message);
  }
  
  print("等待 PRIMARY... (尝试 " + (attempts + 1) + "/30)");
  sleep(3000);
  attempts++;
}

print("超时，未找到 PRIMARY");
exit(1);

EOF
