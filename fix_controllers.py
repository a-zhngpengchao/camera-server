#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复Controller文件的乱码并添加Swagger注解
"""

import os
import re

# 乱码到正确中文的映射表（从之前观察到的模式）
GARBLED_MAP = {
    'IPC 鏈鸿韩鍙风粍鍚堝伐鍏锋帴鍙?': 'IPC 机身号组合工具接口',
    '鏍规嵁鍚勪綅缂栫爜鎷煎嚭瀹屾暣鏈鸿韩鍙?': '根据各位编码拼出完整机身号',
    '绀轰緥锛?': '示例:',
    '缁忛攢鍟嗙鐞嗘帴鍙?': '经销商管理接口',
    '鑾峰彇鍚敤鐨勭粡閿€鍟嗗垪琛?': '获取启用的经销商列表',
    '鑾峰彇鎵€鏈夌粡閿€鍟嗗垪琛紙鍖呭惈绂佺敤鐨勶級': '获取所有经销商列表(包含禁用的)',
    '鑾峰彇鍗曚釜缁忛攢鍟嗚鎯?': '获取单个经销商详情',
    '鏂板缁忛攢鍟?': '新增经销商',
    '璇锋眰浣擄細': '请求体:',
    '缁忛攢鍟嗗悕绉?': '经销商名称',
    '鑱旂郴浜?': '联系人',
    '鍦板潃': '地址',
    '鏇存柊缁忛攢鍟嗕俊鎭?': '更新经销商信息',
    '鍒犻櫎缁忛攢鍟?': '删除经销商',
    '鍚敤/绂佺敤缁忛攢鍟?': '启用/禁用经销商',
    '涓嶈兘涓虹┖': '不能为空',
    '缁忛攢鍟嗕笉瀛樺湪': '经销商不存在',
    'App 閫氱敤鎺ュ彛': 'App 通用接口',
    '鍖呭惈锛?': '包含:',
    '鐗堟湰妫€鏌?': '版本检查',
    '鎻愪氦鍙嶉': '提交反馈',
    'platform 鍜?current_version 涓嶈兘涓虹┖': 'platform 和 current_version 不能为空',
    'content 涓嶈兘涓虹┖': 'content 不能为空',
    '鏈櫥褰?': '未登录',
    '鐢ㄦ埛涓嶅瓨鍦?': '用户不存在',
    '鏂囦欢涓嶈兘涓虹┖': '文件不能为空',
    '涓婁紶澶辫触': '上传失败',
    '鏃犳硶鍒涘缓涓婁紶鐩綍': '无法创建上传目录',
    '鏈嶅姟鍣ㄩ敊璇?': '服务器错误',
    'account 鍜?password 涓嶈兘涓虹┖': 'account 和 password 不能为空',
    'refresh_token 涓嶈兘涓虹┖': 'refresh_token 不能为空',
    'code 涓嶈兘涓虹┖': 'code 不能为空',
    'identity_token 涓嶈兘涓虹┖': 'identity_token 不能为空',
    'id_token 涓嶈兘涓虹┖': 'id_token 不能为空',
    '鐭俊鍙戦€佹殏鏈疄鐜?': '短信发送暂未实现',
    '鐭俊鐧诲綍鏆傛湭瀹炵幇': '短信登录暂未实现',
    '娑堟伅涓嶅瓨鍦?': '消息不存在',
    '鏍囪鎴愬姛': '标记成功',
    '鍒犻櫎鎴愬姛': '删除成功',
    '璁惧涓嶅瓨鍦?': '设备不存在',
    'device_id 涓嶈兘涓虹┖': 'device_id 不能为空',
    '鏃犳潈鎿嶄綔璇ヨ澶?': '无权操作该设备',
    '鏃犳潈鏌ョ湅璇ヨ澶?': '无权查看该设备',
    'direction 涓嶈兘涓虹┖': 'direction 不能为空',
    '鍙戦€?PTZ 鎸囦护澶辫触: ': '发送 PTZ 指令失败: ',
    '褰撳墠浠呮敮鎸?protocol = webrtc': '当前仅支持 protocol = webrtc',
    'stream_id 涓嶈兘涓虹┖': 'stream_id 不能为空',
    '鐩存挱娴佸凡鍋滄': '直播流已停止',
    'product_type 鍜?product_id 涓嶈兘涓虹┖': 'product_type 和 product_id 不能为空',
    '鏃犳潈鎿嶄綔璇ヨ澶?': '无权操作该设备',
    '浜戝瓨鍌ㄥ椁愪笉瀛樺湪': '云存储套餐不存在',
    '鏆備笉鏀寔鐨勫晢鍝佺被鍨?': '暂不支持的商品类型',
    'order_id 涓嶈兘涓虹┖': 'order_id 不能为空',
    '璁㈠崟涓嶅瓨鍦?': '订单不存在',
}

def fix_garbled_text(content):
    """修复乱码文本"""
    for garbled, correct in GARBLED_MAP.items():
        content = content.replace(garbled, correct)
    return content

def add_swagger_imports(content):
    """添加Swagger导入"""
    if 'import io.swagger.v3.oas.annotations' in content:
        return content
    
    # 在package声明后添加import
    pattern = r'(package com\.pura365\.camera\.controller\.\w+;)'
    replacement = r'\1\n\nimport io.swagger.v3.oas.annotations.Operation;\nimport io.swagger.v3.oas.annotations.tags.Tag;'
    content = re.sub(pattern, replacement, content)
    return content

def add_tag_annotation(content, package_type):
    """添加@Tag注解到类上"""
    if '@Tag(' in content:
        return content
    
    # 根据不同的controller类型添加不同的tag
    tag_map = {
        'admin': {
            'DeviceIdToolController': ('设备ID工具', '设备机身号组合工具相关接口'),
            'VendorController': ('经销商管理', '经销商信息管理相关接口'),
            'DeviceProductionController': ('设备生产管理', '装机商管理、生产批次管理、设备管理相关接口'),
        },
        'app': {
            'AppController': ('App通用接口', 'App版本检查、反馈等通用接口'),
            'AuthController': ('用户认证', '用户注册、登录、登出等认证相关接口'),
            'MessageController': ('消息管理', '用户消息查询、标记、删除等接口'),
            'UserController': ('用户信息', '用户信息查询和更新接口'),
            'PaymentController': ('支付管理', '订单创建、支付相关接口'),
        },
        'device': {
            'DeviceController': ('设备管理', '设备增删改查、设置等管理接口'),
            'StreamController': ('视频流管理', '设备直播流启动和停止接口'),
            'NetworkConfigController': ('网络配置', '设备网络配置相关接口'),
            'PairConfigController': ('配对配置', '设备配对相关接口'),
            'VideoPlaybackController': ('视频回放', '视频回放相关接口'),
            'WifiController': ('WiFi管理', 'WiFi配置相关接口'),
        },
        'internal': {
            'CameraController': ('摄像头内部接口', '摄像头设备内部调用接口'),
            'CloudController': ('云服务接口', '云服务相关内部接口'),
            'DataChannelController': ('数据通道', '数据通道相关接口'),
            'MqttControlController': ('MQTT控制', 'MQTT消息控制接口'),
            'WebRtcDebugController': ('WebRTC调试', 'WebRTC调试接口'),
        }
    }
    
    # 查找类名
    class_match = re.search(r'public class (\w+Controller)', content)
    if not class_match:
        return content
    
    class_name = class_match.group(1)
    
    # 获取tag信息
    if package_type in tag_map and class_name in tag_map[package_type]:
        tag_name, tag_desc = tag_map[package_type][class_name]
        
        # 在@RestController前添加@Tag
        pattern = r'(@RestController)'
        replacement = f'@Tag(name = "{tag_name}", description = "{tag_desc}")\n\\1'
        content = re.sub(pattern, replacement, content, count=1)
    
    return content

def fix_request_mapping(content, package_type):
    """统一RequestMapping路径"""
    # 根据package类型修正路径前缀
    prefix_map = {
        'admin': '/api/admin/',
        'app': '/api/app/',
        'device': '/api/device/',
        'internal': '/api/internal/',
    }
    
    if package_type not in prefix_map:
        return content
    
    prefix = prefix_map[package_type]
    
    # 匹配当前的RequestMapping
    pattern = r'@RequestMapping\("(/api/[^"]+)"\)'
    
    def replace_mapping(match):
        current_path = match.group(1)
        # 提取最后的路径部分
        path_parts = current_path.split('/')
        last_part = path_parts[-1] if path_parts else ''
        
        # 重新构建路径
        new_path = prefix + last_part
        return f'@RequestMapping("{new_path}")'
    
    content = re.sub(pattern, replace_mapping, content)
    return content

def process_controller_file(file_path, package_type):
    """处理单个controller文件"""
    print(f"Processing {file_path}...")
    
    try:
        # 读取文件
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 修复乱码
        content = fix_garbled_text(content)
        
        # 添加Swagger导入
        content = add_swagger_imports(content)
        
        # 添加Tag注解
        content = add_tag_annotation(content, package_type)
        
        # 统一RequestMapping路径
        content = fix_request_mapping(content, package_type)
        
        # 写回文件
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✓ Fixed {file_path}")
        return True
    except Exception as e:
        print(f"✗ Error processing {file_path}: {e}")
        return False

def main():
    base_dir = r'D:\workspace\camera-server\src\main\java\com\pura365\camera\controller'
    
    packages = {
        'admin': ['DeviceIdToolController.java', 'DeviceProductionController.java', 'VendorController.java'],
        'app': ['AppController.java', 'AuthController.java', 'MessageController.java', 'UserController.java', 'PaymentController.java'],
        'device': ['DeviceController.java', 'NetworkConfigController.java', 'PairConfigController.java', 'StreamController.java', 'VideoPlaybackController.java', 'WifiController.java'],
        'internal': ['CameraController.java', 'CloudController.java', 'DataChannelController.java', 'MqttControlController.java', 'WebRtcDebugController.java'],
    }
    
    success_count = 0
    failed_count = 0
    
    for package_type, files in packages.items():
        package_dir = os.path.join(base_dir, package_type)
        print(f"\n=== Processing {package_type} package ===")
        
        for file_name in files:
            file_path = os.path.join(package_dir, file_name)
            if os.path.exists(file_path):
                if process_controller_file(file_path, package_type):
                    success_count += 1
                else:
                    failed_count += 1
            else:
                print(f"✗ File not found: {file_path}")
                failed_count += 1
    
    print(f"\n=== Summary ===")
    print(f"Success: {success_count}")
    print(f"Failed: {failed_count}")

if __name__ == '__main__':
    main()
