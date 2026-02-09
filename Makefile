# Maingraph for Minecraft (MGMC) Makefile
# 快速启动与构建脚本

GRADLEW = ./gradlew

.PHONY: fabric neoforge run-fabric run-neoforge clean build help

# 默认显示帮助
all: help

# 运行 Fabric 客户端
fabric: run-fabric
run-fabric:
	$(GRADLEW) :fabric:runClient

# 运行 NeoForge 客户端
neoforge: run-neoforge
run-neoforge:
	$(GRADLEW) :neoforge:runClient

# 清理项目
clean:
	$(GRADLEW) clean

# 构建项目
build:
	$(GRADLEW) build

# 帮助信息
help:
	@echo "MGMC 快速启动脚本"
	@echo "用法:"
	@echo "  make fabric      - 启动 Fabric 客户端"
	@echo "  make neoforge    - 启动 NeoForge 客户端"
	@echo "  make build       - 构建所有平台的 jar 包"
	@echo "  make clean       - 清理构建缓存"
	@echo "  make help        - 显示此帮助信息"
