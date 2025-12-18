#!/bin/sh
# MongoDB 副本集初始化脚本

echo "等待 MongoDB 节点启动..."
sleep 15

echo "初始化副本集..."
# 使用网络别名连接 mongo1.local（容器内可解析）
mongosh --host mongo1.local:27017 --username admin --password 123456 --authenticationDatabase admin <<EOF
// 获取 WSL2 宿主机 IP，用于外部连接
var wslIp = "$(hostname -I | awk '{print \$1}')";
print("WSL2 IP: " + wslIp);

// 使用 MongoDB 5.0+ 的双地址功能
try {
    rs.initiate({
        _id: "app",
        version: 1,
        members: [
            {
                _id: 0,
                host: "mongo1.local:27017",
                tags: { externalHost: wslIp + ":27020" }  # 外部连接地址
            },
            {
                _id: 1,
                host: "mongo2.local:27017",
                tags: { externalHost: wslIp + ":27018" }  # 外部连接地址
            },
            {
                _id: 2,
                host: "mongo3.local:27017",
                tags: { externalHost: wslIp + ":27019" }  # 外部连接地址
            }
        ]
    });
    print("副本集初始化成功");
} catch (e) {
    print("初始化失败，尝试重新配置:", e.message);
    try {
        // 强制重新配置
        cfg = {
            _id: "app",
            version: 2,
            members: [
                {
                    _id: 0,
                    host: "mongo1.local:27017",
                    tags: { externalHost: wslIp + ":27020" }
                },
                {
                    _id: 1,
                    host: "mongo2.local:27017",
                    tags: { externalHost: wslIp + ":27018" }
                },
                {
                    _id: 2,
                    host: "mongo3.local:27017",
                    tags: { externalHost: wslIp + ":27019" }
                }
            ]
        };
        rs.reconfig(cfg, {force: true});
        print("副本集重新配置成功");
    } catch (e2) {
        print("重新配置失败:", e2.message);
    }
}

// 等待成为主节点
sleep(5000);

// 启用外部主机名功能（MongoDB 5.0+）
try {
    db.adminCommand({
        setParameter: 1,
        replicaSetMonitorTimeout: 30,
        replicaSetMonitorMaxFailedChecks: 5
    });
    print("已启用副本集外部主机名功能");
} catch (e) {
    print("启用外部主机名功能失败（可能是旧版本）:", e.message);
}

// 创建用户和数据库
db = db.getSiblingDB("admin");

try {
    db.createUser({
        user: "admin",
        pwd: "123456",
        roles: [
            {role: "root", db: "admin"},
            {role: "readWrite", db: "mpcs-dev"}
        ]
    });
    print("用户 'admin' 创建成功");
} catch (e) {
    print("用户创建跳过:", e.message);
}

// 创建应用数据库
db = db.getSiblingDB("mpcs-dev");
try {
    db.createCollection("init_collection");
    db.init_collection.insertOne({initialized: true, timestamp: new Date()});
    print("数据库 'mpcs-dev' 准备就绪");
} catch(e) {
    print("数据库初始化跳过:", e.message);
}

print("初始化完成");
EOF

echo "验证副本集状态..."
mongosh --host mongo1.local:27017 --username admin --password 123456 --authenticationDatabase admin --eval "rs.status().members.forEach(m => print(m.host + ' - ' + m.stateStr))"

echo "测试外部连接..."
# 测试从容器内部连接到宿主机IP
wslIp=$(hostname -I | awk '{print $1}')
mongosh --host $wslIp:27020 --username admin --password 123456 --authenticationDatabase admin --eval "try { db.adminCommand('ping'); print('✅ 外部连接成功！') } catch(e) { print('❌ 外部连接失败:', e.message) }"

echo "MongoDB 副本集初始化完成"
echo ""
echo "应用连接字符串（在 Windows 中使用）："
echo "mongodb://admin:123456@localhost:27020,localhost:27018,localhost:27019/mpcs-dev?replicaSet=app&authSource=admin"
echo ""
echo "如果使用 localhost 连接失败，请使用 WSL2 IP："
echo "mongodb://admin:123456@$wslIp:27020,$wslIp:27018,$wslIp:27019/mpcs-dev?replicaSet=app&authSource=admin"