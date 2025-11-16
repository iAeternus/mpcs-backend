// 初始化副本集
try {
    rs.initiate({
        _id: "rs0",
        members: [
            { _id: 0, host: "localhost:27017" }
        ]
    });
} catch (e) {
    print("Replica set may already be initialized:", e);
}

// 等待副本集变为 PRIMARY
let attempt = 0;
while (attempt < 10) {
    let status = rs.status();
    if (status.ok === 1 && status.members && status.members[0].stateStr === "PRIMARY") {
        break;
    }
    sleep(1000);
    attempt++;
}

print("Replica set initialized.");

// 切换到 admin 数据库，创建用户
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
    print("User 'admin' created successfully.");
} catch (e) {
    print("User creation skipped (maybe already exists):", e);
}

// 创建 mpcs-dev 数据库（如果不存在）
db = db.getSiblingDB("mpcs-dev");
db.createCollection("init_collection");
print("Database 'mpcs-dev' ready.");
