import http from 'k6/http';
import {check} from 'k6';
import {SharedArray} from 'k6/data';

export const options = {
    vus: 100,
    iterations: 100000,
};

const contentIds = new SharedArray('balanced-content-ids', generateBalancedContentIds);

function generateBalancedContentIds() {
    const sectionSize = 100000;
    const sampleSizePerSection = 25000;
    const all = [];

    for (let section = 0; section < 4; section++) {
        const start = section * sectionSize + 1;
        const end = (section + 1) * sectionSize;
        const sectionIds = new Set();

        while (sectionIds.size < sampleSizePerSection) {
            const id = Math.floor(Math.random() * (end - start + 1)) + start;
            sectionIds.add(id);
        }

        all.push(...sectionIds);
    }

    return shuffle([...all]);
}

function shuffle(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
}

export default function () {
    const headers = {'Content-Type': 'application/json'};
    const url = 'http://localhost:8080/terms/check';

    const contentId = contentIds[__ITER % contentIds.length];

    const res = http.post(
        url,
        JSON.stringify({contentId, earlyReturn: false}),
        {headers}
    );

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response has hasForbiddenTerm': (r) => {
            try {
                const parsed = JSON.parse(r.body);
                return typeof parsed?.data?.hasForbiddenTerm === 'boolean';
            } catch {
                return false;
            }
        },
    });
}
