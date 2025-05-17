import platform
import time
import matplotlib.pyplot as plt
import psycopg2
from psycopg2.pool import ThreadedConnectionPool

# macOS에서 기본 한글 폰트 지정
if platform.system() == 'Darwin':
    plt.rcParams['font.family'] = 'AppleGothic'
elif platform.system() == 'Windows':
    plt.rcParams['font.family'] = 'Malgun Gothic'
else:  # Linux
    plt.rcParams['font.family'] = 'NanumGothic'

# 마이너스 깨짐 방지
plt.rcParams['axes.unicode_minus'] = False

# DB 연결 정보
conn_info = {
    "host": "localhost",
    "port": 5432,
    "dbname": "test",
    "user": "postgres",
    "password": "postgres"
}

# 한글 포함 테스트 입력값 일부 예시
test_inputs = [
    "씨발 진짜 뭐야 지금 이딴 상황이 말이 된다고 생각하냐?",
    "그 새끼 또 지랄하네 아주 그냥 하루도 조용할 날이 없어",
    "좆같은 상황이네 이거 대체 누가 처리한 거냐 진심 어이없다",
    "이딴 병신같은 코드로 운영을 돌리겠다는 놈이 바로 너냐?",
    "아가리 닥쳐라 좀 니 말은 들을 가치도 없어",
    "개새끼가 또 왔어 지가 뭐라도 되는 줄 아나봐",
    "너무 더러운 새끼가 팀 분위기 다 망치고 있음",
    "미친놈 또 오류냄 진짜 정신 상태가 궁금하다",
    "똘아이 같은 짓거리 이제 좀 그만해라 제발",
    "좆도 모름 진짜 기본도 안 된 놈이 까불고 있어",
    "야 이 병신아 넌 왜 항상 그렇게 똥 같은 결과만 내냐",
    "씨팔 그냥 다 때려치고 싶게 만든다 진짜",
    "지랄하네 이게 니가 말하는 완성품이냐?",
    "좆같은 말만 씨부리고 있네 니가 한 게 뭔데",
    "닥쳐 좀 말 그만해 입만 열면 구라야",
    "뒤질래 진짜로 한 번만 더 그러면 확 그냥",
    "엿먹어 너 같은 새끼 때문에 다 꼬였잖아",
    "개지랄 떨고 앉았네 누구 좋으라고 쇼야?",
    "좆같네 진짜 볼 때마다 스트레스다",
    "씨발새끼가 또 시작이네 안 질려?",
    "이딴 좆같은 레포트 누가 쓰랬냐? 정신 있는 거 맞냐",
    "이 새끼는 회의만 하면 뇌절함 아주 천재병 걸렸어",
    "매번 늦고 틀리고 변명만 하는 개씹새끼",
    "그 입 닫고 니 일이나 제대로 해 개같은 놈아",
    "지가 뭐라도 되는 줄 아는 좆밥 새끼",
    "틀딱같은 소리하지 말고 최신 기술 좀 공부해라",
    "개버러지 같은 마인드로 뭘 하겠다는 건데",
    "조센징 마인드 그 자체라 발전이 없음",
    "뒤져라 진짜 제발 좀 사라져줘",
    "정신병자 새끼랑 일하니까 내가 미쳐버릴 것 같음",
    "What the fuck is this garbage? Who signed off on this crap?",
    "You're such a dumbass, it physically hurts to read your code.",
    "Holy shit, how did you manage to break everything in one go?",
    "Goddamn idiot strikes again with another worthless idea.",
    "This whole thing is a fucking disaster from start to finish.",
    "Why does that asshole always have to open his big mouth?",
    "Piece of crap system just crashed again. Great job, genius.",
    "Shut the fuck up and let the real devs handle this.",
    "You're a total jackass for pushing that to production.",
    "This stupid shit shouldn’t have passed any kind of review.",
    "Every time you touch the code, something else breaks, dumb fuck.",
    "Screw you and your pathetic excuses for bad code.",
    "Can someone fire this lazy fuck already?",
    "He talks big but delivers absolutely jack shit. Fucking clown.",
    "That’s the dumbest fucking suggestion I’ve ever heard.",
    "You managed to mess it up again, you useless asshole.",
    "Who the hell wrote this bullshit? It’s unreadable.",
    "Another day, another clusterfuck courtesy of this dipshit.",
    "She acts like a queen but codes like a damn amateur. What a bitch.",
    "It's like he actively tries to be the most annoying jerk around.",
    "You're such a fucking moron it’s almost impressive.",
    "I can’t believe this prick is still on the team.",
    "You absolute twat, do you even test your code?",
    "This is why we can’t have nice things. Thanks, shithead.",
    "Goddammit, fix your mess before blaming others, dickwad.",
    "Your code is trash, your logic is shit, and you're a joke.",
    "Yet another day ruined by that cocksmoker’s merge.",
    "You’re a toxic, arrogant douchebag and we all know it.",
    "You call that documentation? Fuck outta here.",
    "That update was a steaming pile of crap. Nice work, asshole.",
    "진짜 이게 말이 되냐? fuckin server keeps crashing and 그 새끼는 지가 뭘 잘못했는지도 모르고 그냥 헛소리만 씨부리네. every time we deploy, this bullshit happens and no one takes 책임. 씨발 이따위로 일하면 누가 같이 하고 싶겠냐? asshole 같은 코드에 에러 메시지도 지멋대로야. 진심 좆같은 상황이 반복되고 있다. debugging 하다가 미쳐버릴 지경. grow up, you fucking morons. 니네 때문에 밤새고 있는 팀원 생각 좀 해봐라. 병신같은 deployment process부터 다시 뜯어고쳐야 돼. stop acting like a goddamn idiot. 나중에 postmortem 때 니 이름만 적힐거다.",

    "오늘도 어김없이 좆같은 이슈가 떴다. what the actual fuck is going on? 코드 리뷰 안 했냐? 누가 이딴걸 approve 했는지 이해가 안 된다. why the hell are we even deploying this garbage? 테스트도 안 하고 그냥 올려버리는건가? 진짜 retard 같은 move다. 로그는 또 왜 이렇게 병신같이 찍혀있냐? asshole 코드 좀 정리 좀 해라. every time I read your code, I lose faith in humanity. 이딴 코드 관리하는 내가 불쌍할 지경이다. 씨발, 차라리 처음부터 다시 짜자.",

    "하 씨발, 이거 또 뭐야. this bug is so fucking dumb that I’m actually laughing. 개새끼야, 니가 짠 코드 다시 봐봐. does it even compile? 코드 구조는 완전 좆망이고, 주석은 없음, 로직은 뇌절이고. if this was a joke, it wouldn’t be funny. 너같은 병신이 팀에 있다는 게 믿기지 않는다. fuck off and stop pretending you’re helping. your presence is a net negative. 씨부랄, 내가 이딴 걸 고치고 있어야 하냐. 진짜 눈물이 난다.",

    "why is this project turning into such a clusterfuck? 좆도 계획 안 하고 시작하니까 당연히 이 지경이지. 누가 스펙 만들었냐? this spec is a fucking joke. 시간도 없는데 기능은 계속 추가하고, 테스트는 안 하고, 병신같은 스프린트만 반복하고 있음. if anyone thinks this is sustainable, they’re a goddamn lunatic. 좆같은 문화부터 고치자. 그리고 please, stop acting like you know what you're doing. you clearly don't. 씨발 말귀도 못 알아듣는 새끼들이랑은 일 못하겠다.",

    "야 진짜 stop pushing untested shit to main. 좆밥같은 merge 때문에 지금 staging 다 뒤졌어. it took me 2 hours to revert all the crap you deployed. 니가 개발자냐? 개판 만드는 기계지. dumb fuck, pull request는 리뷰 좀 받고 보내라. 왜 이렇게 개념이 없냐. this is a fuckin nightmare. 자꾸 이러면 진짜 팀에서 잘릴 줄 알아. 회사가 장난이냐 병신아? 씨발, 니 코드 볼 때마다 내 혈압이 오른다.",

    "오늘도 역시 씨발같은 일정. who the hell approved this deadline? this is fucking suicide. 일도 안 하고 구라만 치는 새끼들이 결정권자니까 당연하지. every decision is a goddamn disaster. 누가 이렇게 병신같은 구조로 설계했냐? 팀장이라는 새끼는 왜 항상 없는 회의만 나가고 일은 안 하냐? stop lying and do your fucking job. 욕밖에 안 나온다 진짜. 좆같은 현실이다.",

    "you call this a fucking system? 이딴 걸 시스템이라고 우기고 앉았네. database 설계부터 완전 좆망이고, API는 병신같이 동작하고, error handling은 그냥 없어. this is not engineering, this is clowning. 팀원들 다 지쳤다. we’re all tired of this bullshit. 병신같은 지시만 받고 진짜 일하는 사람은 피말리고. 씨발, 여기서 일하는 게 형벌이다. quit acting like you know what you're doing. 니가 문제의 근원이야.",

    "시발 오늘도 그새끼 때문에 회의가 폭파됐다. why the fuck does he always talk over people? 진짜 개같이 무례하고, 자기 말만 맞다고 우기고. asshole, learn to shut up for once. we can’t even get through an agenda without your drama. 좆같은 태도 고치라고 몇 번을 말하냐. no one respects a loud idiot. it’s not leadership, it’s just being a dick. 씨발, 스트레스 받는다 진짜.",

    "테스트도 안 했는데 merge해버리는 용기는 어디서 나오냐? do you have a death wish or something? dev 서버 다 망가졌잖아, 개새끼야. this is beyond stupid. pull request 봤냐? 리뷰 안 받고 올리는 놈이 제일 문제다. grow the fuck up. 지 혼자 천재인 줄 아는 좆밥새끼들 때문에 다 같이 피해본다. 너 같은 새끼는 dev 환경에서 손 떼라. seriously, fuck off.",

    "this is not just a bug, it’s a goddamn tragedy. 니가 짠 코드 보니까 진짜 눈물이 난다. 반복문 안에서 I/O, null 체크 없음, 주석은 개소리. 병신도 이 정도는 안 짠다. what kind of idiot writes this trash? 씨발, 이걸 리뷰하면서 내가 사람이라는 사실이 싫어졌다. get your shit together. 우리 프로젝트는 장난이 아니야. 너처럼 대충 하는 새끼 때문에 다 같이 망하는 거야.",

    "야 이 병신아, 브랜치 이름 좀 의미있게 짓자. what the fuck is 'update-123-draft2-final' supposed to mean? 좆같은 네이밍 때문에 이슈 추적도 못하고 협업도 안 됨. be a professional, not a fucking clown. 코드 품질이 중요한 게 아니라 기본이 안 돼 있음. 병신같은 variable 이름부터 class 구조까지 다 뜯어고쳐야 해. 씨발, 무슨 코드가 다 이따위냐?",

    "사람이 실수할 수는 있지, but you fuck up literally every time. 테스트 실패하면 고치고 다시 보내는 게 기본인데, 넌 매번 무시하고 머지함. 좆도 신경 안 쓰는 티 팍팍 남. we’re not your babysitters. 니 병신같은 실수 때문에 시간 낭비하는 거 지겹다. do it right or don’t fucking do it at all. 계속 이러면 진짜 팀에서 짤릴 줄 알아라.",

    "fuckin API 응답 왜 이따구냐? 응답 속도는 거북이보다 느리고, 필드 빠져 있고, 오류 메시지는 지 맘대로고. 이런 병신같은 응답은 개발자를 고문하려는 거냐? 씨발, QA도 못한걸 왜 운영에 배포함? 니가 만든 이 좆같은 백엔드는 기적적으로도 작동하지 않는다. just admit you don’t know what you’re doing and step aside.",

    "야, 이게 니가 말한 완성품이냐? fuck outta here with that bullshit. 누가 봐도 beta도 안 된 걸 갖고 와서 대충 설명하고 끝? 니가 하는 일은 구라와 병신같은 핑계뿐이다. 제대로 된 프로세스도 없고, 이슈 관리도 개판, 디버깅도 좆같이 해. what a waste of resources. get lost.",

    "이 정도면 너는 그냥 fuckery generator다. 일만 맡기면 늘 병신같은 일이 생기지. 누가 이렇게 매번 문제를 일으킬 수 있냐? 코드 리뷰 안 하고, 테스트 안 하고, 로그도 안 찍고 배포한다? 이건 일부러 망치려는 수준이다. 씨팔, 진짜 짜증나. if I had a dollar for every time you fucked up, I'd be a millionaire.",

    "좆도 모르는 새끼가 말은 존나게 많네. you haven’t shipped a single feature in weeks but you got opinions on everything? shut the fuck up and start working. 회의 시간에 입만 털지 말고 코드 좀 짜라. 이딴 팀원 하나 있는 것만으로도 프로젝트가 좆된다. nobody likes a freeloading asshole.",

    "이건 그냥 disaster가 아니라 완전 fuckageddon이다. 지금 backend랑 frontend API가 완전히 따로 놀고 있는데, 니가 만든 거라고? 씨발, 차라리 안 쓰는 게 낫겠다. rollback할게. 야, 니가 만든 거 싹 다 치워. it’s not just bad, it’s unusable. 진짜 욕 나올 수준이다.",

    "every day I wonder how you’re still employed. 니가 만든 코드, 테스트 한번도 안 통과했고, 이슈만 존나게 남김. 병신같이 resource 누수나는 거 고치라는 말은 들은 적도 없지? this isn’t your playground. it’s production, dumbass. stop deploying bullshit.",

    "this isn’t a fucking startup playground, 이건 운영 서비스라고. 병신같이 dev 서버에 실험적인 거 올리지 마. every time you ‘experiment’, something breaks. you think you’re some genius but you’re just a pain in the ass. 좆같은 책임감 없는 행동은 이제 그만하자.",

    "너는 정말 consistently terrible 하다. 병신같은 merge commit은 보기도 싫고, revert하기도 빡세고, 매번 conflict 일으킴. is it really that hard to follow instructions? 니가 팀의 productivity를 갉아먹고 있음. 씨팔, 누가 너를 hire 했냐?",
    "진짜 이게 말이 되냐? fuckin server keeps crashing and 그 새끼는 지가 뭘 잘못했는지도 모르고 그냥 헛소리만 씨부리네. every time we deploy, this bullshit happens and no one takes 책임. 씨발 이따위로 일하면 누가 같이 하고 싶겠냐? asshole 같은 코드에 에러 메시지도 지멋대로야. 진심 좆같은 상황이 반복되고 있다. debugging 하다가 미쳐버릴 지경. grow up, you fucking morons. 니네 때문에 밤새고 있는 팀원 생각 좀 해봐라. 병신같은 deployment process부터 다시 뜯어고쳐야 돼. stop acting like a goddamn idiot. 나중에 postmortem 때 니 이름만 적힐거다.",

    "오늘도 어김없이 좆같은 이슈가 떴다. what the actual fuck is going on? 코드 리뷰 안 했냐? 누가 이딴걸 approve 했는지 이해가 안 된다. why the hell are we even deploying this garbage? 테스트도 안 하고 그냥 올려버리는건가? 진짜 retard 같은 move다. 로그는 또 왜 이렇게 병신같이 찍혀있냐? asshole 코드 좀 정리 좀 해라. every time I read your code, I lose faith in humanity. 이딴 코드 관리하는 내가 불쌍할 지경이다. 씨발, 차라리 처음부터 다시 짜자.",

    "하 씨발, 이거 또 뭐야. this bug is so fucking dumb that I’m actually laughing. 개새끼야, 니가 짠 코드 다시 봐봐. does it even compile? 코드 구조는 완전 좆망이고, 주석은 없음, 로직은 뇌절이고. if this was a joke, it wouldn’t be funny. 너같은 병신이 팀에 있다는 게 믿기지 않는다. fuck off and stop pretending you’re helping. your presence is a net negative. 씨부랄, 내가 이딴 걸 고치고 있어야 하냐. 진짜 눈물이 난다.",

    "why is this project turning into such a clusterfuck? 좆도 계획 안 하고 시작하니까 당연히 이 지경이지. 누가 스펙 만들었냐? this spec is a fucking joke. 시간도 없는데 기능은 계속 추가하고, 테스트는 안 하고, 병신같은 스프린트만 반복하고 있음. if anyone thinks this is sustainable, they’re a goddamn lunatic. 좆같은 문화부터 고치자. 그리고 please, stop acting like you know what you're doing. you clearly don't. 씨발 말귀도 못 알아듣는 새끼들이랑은 일 못하겠다.",

    "야 진짜 stop pushing untested shit to main. 좆밥같은 merge 때문에 지금 staging 다 뒤졌어. it took me 2 hours to revert all the crap you deployed. 니가 개발자냐? 개판 만드는 기계지. dumb fuck, pull request는 리뷰 좀 받고 보내라. 왜 이렇게 개념이 없냐. this is a fuckin nightmare. 자꾸 이러면 진짜 팀에서 잘릴 줄 알아. 회사가 장난이냐 병신아? 씨발, 니 코드 볼 때마다 내 혈압이 오른다.",

    "오늘도 역시 씨발같은 일정. who the hell approved this deadline? this is fucking suicide. 일도 안 하고 구라만 치는 새끼들이 결정권자니까 당연하지. every decision is a goddamn disaster. 누가 이렇게 병신같은 구조로 설계했냐? 팀장이라는 새끼는 왜 항상 없는 회의만 나가고 일은 안 하냐? stop lying and do your fucking job. 욕밖에 안 나온다 진짜. 좆같은 현실이다.",

    "you call this a fucking system? 이딴 걸 시스템이라고 우기고 앉았네. database 설계부터 완전 좆망이고, API는 병신같이 동작하고, error handling은 그냥 없어. this is not engineering, this is clowning. 팀원들 다 지쳤다. we’re all tired of this bullshit. 병신같은 지시만 받고 진짜 일하는 사람은 피말리고. 씨발, 여기서 일하는 게 형벌이다. quit acting like you know what you're doing. 니가 문제의 근원이야.",

    "시발 오늘도 그새끼 때문에 회의가 폭파됐다. why the fuck does he always talk over people? 진짜 개같이 무례하고, 자기 말만 맞다고 우기고. asshole, learn to shut up for once. we can’t even get through an agenda without your drama. 좆같은 태도 고치라고 몇 번을 말하냐. no one respects a loud idiot. it’s not leadership, it’s just being a dick. 씨발, 스트레스 받는다 진짜.",

    "테스트도 안 했는데 merge해버리는 용기는 어디서 나오냐? do you have a death wish or something? dev 서버 다 망가졌잖아, 개새끼야. this is beyond stupid. pull request 봤냐? 리뷰 안 받고 올리는 놈이 제일 문제다. grow the fuck up. 지 혼자 천재인 줄 아는 좆밥새끼들 때문에 다 같이 피해본다. 너 같은 새끼는 dev 환경에서 손 떼라. seriously, fuck off.",

    "this is not just a bug, it’s a goddamn tragedy. 니가 짠 코드 보니까 진짜 눈물이 난다. 반복문 안에서 I/O, null 체크 없음, 주석은 개소리. 병신도 이 정도는 안 짠다. what kind of idiot writes this trash? 씨발, 이걸 리뷰하면서 내가 사람이라는 사실이 싫어졌다. get your shit together. 우리 프로젝트는 장난이 아니야. 너처럼 대충 하는 새끼 때문에 다 같이 망하는 거야.",

    "야 이 병신아, 브랜치 이름 좀 의미있게 짓자. what the fuck is 'update-123-draft2-final' supposed to mean? 좆같은 네이밍 때문에 이슈 추적도 못하고 협업도 안 됨. be a professional, not a fucking clown. 코드 품질이 중요한 게 아니라 기본이 안 돼 있음. 병신같은 variable 이름부터 class 구조까지 다 뜯어고쳐야 해. 씨발, 무슨 코드가 다 이따위냐?",

    "사람이 실수할 수는 있지, but you fuck up literally every time. 테스트 실패하면 고치고 다시 보내는 게 기본인데, 넌 매번 무시하고 머지함. 좆도 신경 안 쓰는 티 팍팍 남. we’re not your babysitters. 니 병신같은 실수 때문에 시간 낭비하는 거 지겹다. do it right or don’t fucking do it at all. 계속 이러면 진짜 팀에서 짤릴 줄 알아라.",

    "fuckin API 응답 왜 이따구냐? 응답 속도는 거북이보다 느리고, 필드 빠져 있고, 오류 메시지는 지 맘대로고. 이런 병신같은 응답은 개발자를 고문하려는 거냐? 씨발, QA도 못한걸 왜 운영에 배포함? 니가 만든 이 좆같은 백엔드는 기적적으로도 작동하지 않는다. just admit you don’t know what you’re doing and step aside.",

    "야, 이게 니가 말한 완성품이냐? fuck outta here with that bullshit. 누가 봐도 beta도 안 된 걸 갖고 와서 대충 설명하고 끝? 니가 하는 일은 구라와 병신같은 핑계뿐이다. 제대로 된 프로세스도 없고, 이슈 관리도 개판, 디버깅도 좆같이 해. what a waste of resources. get lost.",

    "이 정도면 너는 그냥 fuckery generator다. 일만 맡기면 늘 병신같은 일이 생기지. 누가 이렇게 매번 문제를 일으킬 수 있냐? 코드 리뷰 안 하고, 테스트 안 하고, 로그도 안 찍고 배포한다? 이건 일부러 망치려는 수준이다. 씨팔, 진짜 짜증나. if I had a dollar for every time you fucked up, I'd be a millionaire.",

    "좆도 모르는 새끼가 말은 존나게 많네. you haven’t shipped a single feature in weeks but you got opinions on everything? shut the fuck up and start working. 회의 시간에 입만 털지 말고 코드 좀 짜라. 이딴 팀원 하나 있는 것만으로도 프로젝트가 좆된다. nobody likes a freeloading asshole.",

    "이건 그냥 disaster가 아니라 완전 fuckageddon이다. 지금 backend랑 frontend API가 완전히 따로 놀고 있는데, 니가 만든 거라고? 씨발, 차라리 안 쓰는 게 낫겠다. rollback할게. 야, 니가 만든 거 싹 다 치워. it’s not just bad, it’s unusable. 진짜 욕 나올 수준이다.",

    "every day I wonder how you’re still employed. 니가 만든 코드, 테스트 한번도 안 통과했고, 이슈만 존나게 남김. 병신같이 resource 누수나는 거 고치라는 말은 들은 적도 없지? this isn’t your playground. it’s production, dumbass. stop deploying bullshit.",

    "this isn’t a fucking startup playground, 이건 운영 서비스라고. 병신같이 dev 서버에 실험적인 거 올리지 마. every time you ‘experiment’, something breaks. you think you’re some genius but you’re just a pain in the ass. 좆같은 책임감 없는 행동은 이제 그만하자.",

    "너는 정말 consistently terrible 하다. 병신같은 merge commit은 보기도 싫고, revert하기도 빡세고, 매번 conflict 일으킴. is it really that hard to follow instructions? 니가 팀의 productivity를 갉아먹고 있음. 씨팔, 누가 너를 hire 했냐?",
    "진짜 이게 말이 되냐? fuckin server keeps crashing and 그 새끼는 지가 뭘 잘못했는지도 모르고 그냥 헛소리만 씨부리네. every time we deploy, this bullshit happens and no one takes 책임. 씨발 이따위로 일하면 누가 같이 하고 싶겠냐? asshole 같은 코드에 에러 메시지도 지멋대로야. 진심 좆같은 상황이 반복되고 있다. debugging 하다가 미쳐버릴 지경. grow up, you fucking morons. 니네 때문에 밤새고 있는 팀원 생각 좀 해봐라. 병신같은 deployment process부터 다시 뜯어고쳐야 돼. stop acting like a goddamn idiot. 나중에 postmortem 때 니 이름만 적힐거다.",

    "오늘도 어김없이 좆같은 이슈가 떴다. what the actual fuck is going on? 코드 리뷰 안 했냐? 누가 이딴걸 approve 했는지 이해가 안 된다. why the hell are we even deploying this garbage? 테스트도 안 하고 그냥 올려버리는건가? 진짜 retard 같은 move다. 로그는 또 왜 이렇게 병신같이 찍혀있냐? asshole 코드 좀 정리 좀 해라. every time I read your code, I lose faith in humanity. 이딴 코드 관리하는 내가 불쌍할 지경이다. 씨발, 차라리 처음부터 다시 짜자.",

    "하 씨발, 이거 또 뭐야. this bug is so fucking dumb that I’m actually laughing. 개새끼야, 니가 짠 코드 다시 봐봐. does it even compile? 코드 구조는 완전 좆망이고, 주석은 없음, 로직은 뇌절이고. if this was a joke, it wouldn’t be funny. 너같은 병신이 팀에 있다는 게 믿기지 않는다. fuck off and stop pretending you’re helping. your presence is a net negative. 씨부랄, 내가 이딴 걸 고치고 있어야 하냐. 진짜 눈물이 난다.",

    "why is this project turning into such a clusterfuck? 좆도 계획 안 하고 시작하니까 당연히 이 지경이지. 누가 스펙 만들었냐? this spec is a fucking joke. 시간도 없는데 기능은 계속 추가하고, 테스트는 안 하고, 병신같은 스프린트만 반복하고 있음. if anyone thinks this is sustainable, they’re a goddamn lunatic. 좆같은 문화부터 고치자. 그리고 please, stop acting like you know what you're doing. you clearly don't. 씨발 말귀도 못 알아듣는 새끼들이랑은 일 못하겠다.",

    "야 진짜 stop pushing untested shit to main. 좆밥같은 merge 때문에 지금 staging 다 뒤졌어. it took me 2 hours to revert all the crap you deployed. 니가 개발자냐? 개판 만드는 기계지. dumb fuck, pull request는 리뷰 좀 받고 보내라. 왜 이렇게 개념이 없냐. this is a fuckin nightmare. 자꾸 이러면 진짜 팀에서 잘릴 줄 알아. 회사가 장난이냐 병신아? 씨발, 니 코드 볼 때마다 내 혈압이 오른다.",

    "오늘도 역시 씨발같은 일정. who the hell approved this deadline? this is fucking suicide. 일도 안 하고 구라만 치는 새끼들이 결정권자니까 당연하지. every decision is a goddamn disaster. 누가 이렇게 병신같은 구조로 설계했냐? 팀장이라는 새끼는 왜 항상 없는 회의만 나가고 일은 안 하냐? stop lying and do your fucking job. 욕밖에 안 나온다 진짜. 좆같은 현실이다.",

    "you call this a fucking system? 이딴 걸 시스템이라고 우기고 앉았네. database 설계부터 완전 좆망이고, API는 병신같이 동작하고, error handling은 그냥 없어. this is not engineering, this is clowning. 팀원들 다 지쳤다. we’re all tired of this bullshit. 병신같은 지시만 받고 진짜 일하는 사람은 피말리고. 씨발, 여기서 일하는 게 형벌이다. quit acting like you know what you're doing. 니가 문제의 근원이야.",

    "시발 오늘도 그새끼 때문에 회의가 폭파됐다. why the fuck does he always talk over people? 진짜 개같이 무례하고, 자기 말만 맞다고 우기고. asshole, learn to shut up for once. we can’t even get through an agenda without your drama. 좆같은 태도 고치라고 몇 번을 말하냐. no one respects a loud idiot. it’s not leadership, it’s just being a dick. 씨발, 스트레스 받는다 진짜.",

    "테스트도 안 했는데 merge해버리는 용기는 어디서 나오냐? do you have a death wish or something? dev 서버 다 망가졌잖아, 개새끼야. this is beyond stupid. pull request 봤냐? 리뷰 안 받고 올리는 놈이 제일 문제다. grow the fuck up. 지 혼자 천재인 줄 아는 좆밥새끼들 때문에 다 같이 피해본다. 너 같은 새끼는 dev 환경에서 손 떼라. seriously, fuck off.",

    "this is not just a bug, it’s a goddamn tragedy. 니가 짠 코드 보니까 진짜 눈물이 난다. 반복문 안에서 I/O, null 체크 없음, 주석은 개소리. 병신도 이 정도는 안 짠다. what kind of idiot writes this trash? 씨발, 이걸 리뷰하면서 내가 사람이라는 사실이 싫어졌다. get your shit together. 우리 프로젝트는 장난이 아니야. 너처럼 대충 하는 새끼 때문에 다 같이 망하는 거야.",

    "야 이 병신아, 브랜치 이름 좀 의미있게 짓자. what the fuck is 'update-123-draft2-final' supposed to mean? 좆같은 네이밍 때문에 이슈 추적도 못하고 협업도 안 됨. be a professional, not a fucking clown. 코드 품질이 중요한 게 아니라 기본이 안 돼 있음. 병신같은 variable 이름부터 class 구조까지 다 뜯어고쳐야 해. 씨발, 무슨 코드가 다 이따위냐?",

    "사람이 실수할 수는 있지, but you fuck up literally every time. 테스트 실패하면 고치고 다시 보내는 게 기본인데, 넌 매번 무시하고 머지함. 좆도 신경 안 쓰는 티 팍팍 남. we’re not your babysitters. 니 병신같은 실수 때문에 시간 낭비하는 거 지겹다. do it right or don’t fucking do it at all. 계속 이러면 진짜 팀에서 짤릴 줄 알아라.",

    "fuckin API 응답 왜 이따구냐? 응답 속도는 거북이보다 느리고, 필드 빠져 있고, 오류 메시지는 지 맘대로고. 이런 병신같은 응답은 개발자를 고문하려는 거냐? 씨발, QA도 못한걸 왜 운영에 배포함? 니가 만든 이 좆같은 백엔드는 기적적으로도 작동하지 않는다. just admit you don’t know what you’re doing and step aside.",

    "야, 이게 니가 말한 완성품이냐? fuck outta here with that bullshit. 누가 봐도 beta도 안 된 걸 갖고 와서 대충 설명하고 끝? 니가 하는 일은 구라와 병신같은 핑계뿐이다. 제대로 된 프로세스도 없고, 이슈 관리도 개판, 디버깅도 좆같이 해. what a waste of resources. get lost.",

    "이 정도면 너는 그냥 fuckery generator다. 일만 맡기면 늘 병신같은 일이 생기지. 누가 이렇게 매번 문제를 일으킬 수 있냐? 코드 리뷰 안 하고, 테스트 안 하고, 로그도 안 찍고 배포한다? 이건 일부러 망치려는 수준이다. 씨팔, 진짜 짜증나. if I had a dollar for every time you fucked up, I'd be a millionaire.",

    "좆도 모르는 새끼가 말은 존나게 많네. you haven’t shipped a single feature in weeks but you got opinions on everything? shut the fuck up and start working. 회의 시간에 입만 털지 말고 코드 좀 짜라. 이딴 팀원 하나 있는 것만으로도 프로젝트가 좆된다. nobody likes a freeloading asshole.",

    "이건 그냥 disaster가 아니라 완전 fuckageddon이다. 지금 backend랑 frontend API가 완전히 따로 놀고 있는데, 니가 만든 거라고? 씨발, 차라리 안 쓰는 게 낫겠다. rollback할게. 야, 니가 만든 거 싹 다 치워. it’s not just bad, it’s unusable. 진짜 욕 나올 수준이다.",

    "every day I wonder how you’re still employed. 니가 만든 코드, 테스트 한번도 안 통과했고, 이슈만 존나게 남김. 병신같이 resource 누수나는 거 고치라는 말은 들은 적도 없지? this isn’t your playground. it’s production, dumbass. stop deploying bullshit.",

    "this isn’t a fucking startup playground, 이건 운영 서비스라고. 병신같이 dev 서버에 실험적인 거 올리지 마. every time you ‘experiment’, something breaks. you think you’re some genius but you’re just a pain in the ass. 좆같은 책임감 없는 행동은 이제 그만하자.",

    "너는 정말 consistently terrible 하다. 병신같은 merge commit은 보기도 싫고, revert하기도 빡세고, 매번 conflict 일으킴. is it really that hard to follow instructions? 니가 팀의 productivity를 갉아먹고 있음. 씨팔, 누가 너를 hire 했냐?",

]

# 커넥션 풀 설정 (최소 1, 최대 10개 커넥션)
pool = ThreadedConnectionPool(minconn=1, maxconn=10, **conn_info)

execution_times = []
start_all = time.time()

query = "SELECT term FROM public.forbidden_term WHERE LOWER(%s) LIKE '%%' || term || '%%';"

try:
    for input_text in test_inputs:
        conn = pool.getconn()
        try:
            conn.set_client_encoding('UTF8')
            with conn.cursor() as cur:
                start = time.time()
                cur.execute(query, (input_text,))
                safe_input = input_text.replace("'", "''")
                print(
                    f"SELECT term FROM public.forbidden_term WHERE LOWER('{safe_input}') LIKE '%' || term || '%';"
                )
                _ = cur.fetchall()
                elapsed = (time.time() - start) * 1000  # ms
                execution_times.append(elapsed)

            # 트랜잭션 분리용 (autocommit 사용 or rollback)
            conn.rollback()  # SELECT만 하므로 rollback으로 clear

        finally:
            pool.putconn(conn)

except Exception as e:
    print("DB 오류 발생:", str(e))

end_all = time.time()
total_elapsed = (end_all - start_all) * 1000  # 전체 ms

# 평균 계산
avg_time = sum(execution_times) / len(execution_times) if execution_times else 0

# 그래프 출력
plt.figure(figsize=(12, 6))
plt.hist(execution_times, bins=10, color='lightgreen', edgecolor='black')
plt.title(f"한글 금칙어 검사 쿼리 실행 시간 분포 (한영 100~500글자, 총 {len(execution_times)}건)")

plt.xlabel("실행 시간 (ms)")
plt.ylabel("쿼리 수")
plt.grid(True)

plt.text(
    x=max(execution_times) * 0.6,
    y=max(plt.gca().get_ylim()) * 0.9,
    s=f"평균 실행 시간: {avg_time:.2f} ms\n총 실행 시간: {total_elapsed:.2f} ms",
    fontsize=12,
    fontweight='bold',
    color='darkred'
)

plt.tight_layout()
plt.show()

# 마지막에 커넥션 풀 닫기
pool.closeall()
