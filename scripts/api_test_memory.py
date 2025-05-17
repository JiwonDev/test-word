import json
import matplotlib.pyplot as plt
import numpy as np
import platform

# macOS/Windows/Linux 한글 폰트 설정
if platform.system() == 'Darwin':
    plt.rcParams['font.family'] = 'AppleGothic'
elif platform.system() == 'Windows':
    plt.rcParams['font.family'] = 'Malgun Gothic'
else:  # Linux
    plt.rcParams['font.family'] = 'NanumGothic'

# 마이너스 깨짐 방지
plt.rcParams['axes.unicode_minus'] = False

# k6 결과 JSON 로드
with open("/Users/jiwon/PycharmProjects/agent-builder/app/api/results.json", "r") as f:
    data = json.load(f)

# 시각화에 사용할 metric 선택
metric_key = "http_req_duration"
metric = data["metrics"].get(metric_key)

if not metric:
    raise ValueError(f"{metric_key} metric not found in the k6 result")

# 값 추출
avg = metric["avg"]
med = metric["med"]
p90 = metric["p(90)"]
p95 = metric["p(95)"]
min_ = metric["min"]
max_ = metric["max"]

# 예시 데이터 생성 (히스토그램용 - 실제 raw 데이터가 없으므로 랜덤 샘플링)
np.random.seed(42)
sample = np.random.normal(loc=avg, scale=(p95 - med) / 2.5, size=1000)
sample = np.clip(sample, min_, max_)

# 서브플롯 생성: [히스토그램, 요약 통계 라인]
fig, axes = plt.subplots(2, 1, figsize=(12, 10), gridspec_kw={'height_ratios': [3, 1]})
fig.suptitle("k6 http_req_duration 분석", fontsize=16, fontweight='bold')

# 1. 히스토그램
axes[0].hist(sample, bins=20, color='skyblue', edgecolor='black')
axes[0].set_title("응답 시간 분포 (샘플 1000개)")
axes[0].set_xlabel("응답 시간 (ms)")
axes[0].set_ylabel("요청 수")
axes[0].grid(True)

# 기준선 표시
axes[0].axvline(avg, color='red', linestyle='--', label=f"avg: {avg:.2f}ms")
axes[0].axvline(med, color='green', linestyle='--', label=f"med: {med:.2f}ms")
axes[0].axvline(p90, color='orange', linestyle='--', label=f"p90: {p90:.2f}ms")
axes[0].axvline(p95, color='purple', linestyle='--', label=f"p95: {p95:.2f}ms")
axes[0].legend()

# 2. 요약 통계 그래프
x_labels = ["min", "avg", "med", "p90", "p95", "max"]
values = [min_, avg, med, p90, p95, max_]
axes[1].plot(x_labels, values, marker='o', linestyle='-', color='blue')
axes[1].set_title("응답 시간 요약 통계")
axes[1].set_ylabel("응답 시간 (ms)")
axes[1].grid(True)

# 레이아웃 정리 및 출력
plt.tight_layout(rect=[0, 0, 1, 0.95])  # 제목 공간 확보
plt.show()
