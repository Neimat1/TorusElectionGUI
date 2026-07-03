const form = document.querySelector('#election-form');
const rowsInput = document.querySelector('#rows');
const colsInput = document.querySelector('#cols');
const totalInput = document.querySelector('#total');
const idsInput = document.querySelector('#ids');
const errorOutput = document.querySelector('#error');
const canvas = document.querySelector('#torus');
const ctx = canvas.getContext('2d');
let canvasWidth = canvas.width;
let canvasHeight = canvas.height;
const animateButton = document.querySelector('#animate');
const resetButton = document.querySelector('#reset');
const menuToggle = document.querySelector('.menu-toggle');
const menuBackdrop = document.querySelector('.menu-backdrop');
const sidebar = document.querySelector('#sidebar');
const fields = {
    status: document.querySelector('#status'),
    leader: document.querySelector('#leader'),
    position: document.querySelector('#position'),
    rounds: document.querySelector('#roundsOut'),
    messages: document.querySelector('#messages'),
    startTime: document.querySelector('#startTime'),
    endTime: document.querySelector('#endTime'),
    log: document.querySelector('#logOutput')
};

let lastResult = null;
let animationTimer = null;
let displayNodes = [];
let activeStep = null;

function setMenuOpen(open) {
    document.body.classList.toggle('menu-open', open);
    if (menuToggle) {
        menuToggle.setAttribute('aria-expanded', String(open));
        menuToggle.setAttribute('aria-label', open ? 'Close menu' : 'Open menu');
    }
}

if (menuToggle) {
    menuToggle.addEventListener('click', () => setMenuOpen(!document.body.classList.contains('menu-open')));
}
if (menuBackdrop) {
    menuBackdrop.addEventListener('click', () => setMenuOpen(false));
}
if (sidebar) {
    sidebar.addEventListener('click', event => {
        if (event.target.closest('a')) setMenuOpen(false);
    });
}
window.addEventListener('keydown', event => {
    if (event.key === 'Escape') setMenuOpen(false);
});

const colors = {
    link: '#0b78d0',
    processFill: '#e8f4ff',
    processBorder: '#0b78d0',
    senderFill: '#f7c948',
    senderBorder: '#b7791f',
    receiverFill: '#ffd0d0',
    receiverUpdatedFill: '#ef767a',
    receiverBorder: '#c2413d',
    leaderFill: '#61c77b',
    leaderBorder: '#15803d',
    text: '#172033'
};

function updateTotal() {
    totalInput.value = Number(rowsInput.value) * Number(colsInput.value);
}

function parseInput() {
    const rows = Number(rowsInput.value);
    const cols = Number(colsInput.value);
    const ids = idsInput.value.trim().split(/\s+/).filter(Boolean).map(Number);
    if (!Number.isInteger(rows) || !Number.isInteger(cols) || rows < 2 || cols < 2) {
        throw new Error('Rows and columns must be at least 2.');
    }
    if (ids.some(id => !Number.isInteger(id))) {
        throw new Error('Process IDs must be whole numbers.');
    }
    if (ids.length !== rows * cols) {
        throw new Error('Expected ' + (rows * cols) + ' process IDs.');
    }
    if (new Set(ids).size !== ids.length) {
        throw new Error('Process IDs must be unique.');
    }
    return { rows, cols, ids };
}

async function runElection({ animate = false } = {}) {
    stopAnimation();
    errorOutput.textContent = '';
    const payload = parseInput();
    setStatus(animate ? 'Preparing Animation' : 'Running');
    const response = await fetch('/api/election', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || 'Election failed.');
    }
    lastResult = await response.json();
    displayNodes = cloneNodes(lastResult.nodes, animate);
    activeStep = null;
    updateStatus(lastResult, animate ? 'Running Animation' : 'Completed');
    draw(lastResult, displayNodes, activeStep);
    if (animate) {
        playAnimation();
    }
}

function cloneNodes(nodes, initialOnly) {
    return nodes.map(node => ({
        ...node,
        maxKnownId: initialOnly ? node.id : node.maxKnownId,
        leader: initialOnly ? false : node.leader
    }));
}

function updateStatus(result, statusText) {
    setStatus(statusText);
    fields.leader.textContent = result.leaderId;
    fields.position.textContent = '(' + result.leaderPosition.row + ', ' + result.leaderPosition.col + ')';
    fields.rounds.textContent = result.rounds;
    fields.messages.textContent = result.messages;
    fields.startTime.textContent = result.startTime;
    fields.endTime.textContent = statusText === 'Running Animation' ? '-' : result.endTime;
    fields.log.textContent = result.log.join('\n');
}

function playAnimation() {
    let index = 0;
    fields.log.textContent = 'Animation started.\n';
    animationTimer = window.setInterval(() => {
        if (!lastResult || index >= lastResult.steps.length) {
            finishAnimation();
            return;
        }
        activeStep = lastResult.steps[index++];
        const receiver = displayNodes.find(node => node.row === activeStep.receiverPosition.row && node.col === activeStep.receiverPosition.col);
        if (receiver && activeStep.updated) {
            receiver.maxKnownId = activeStep.transmittedValue;
        }
        fields.log.textContent += '[Round ' + activeStep.round + '] P' + activeStep.receiverId + ' reads max=' + activeStep.transmittedValue + ' from P' + activeStep.senderId + (activeStep.updated ? '  UPDATED\n' : '\n');
        fields.log.scrollTop = fields.log.scrollHeight;
        draw(lastResult, displayNodes, activeStep);
    }, 450);
}

function finishAnimation() {
    stopAnimation();
    if (!lastResult) return;
    displayNodes = cloneNodes(lastResult.nodes, false);
    activeStep = null;
    updateStatus(lastResult, 'Completed');
    fields.log.textContent = 'Final summary:\n' + lastResult.log.map(line => '- ' + line).join('\n');
    draw(lastResult, displayNodes, activeStep);
}

function setStatus(value) {
    fields.status.textContent = value;
    fields.status.classList.remove('status-running', 'status-completed');
    fields.leader.classList.remove('status-completed');
    fields.position.classList.remove('status-completed');
    if (value.includes('Running') || value.includes('Preparing')) {
        fields.status.classList.add('status-running');
    } else if (value === 'Completed') {
        fields.status.classList.add('status-completed');
        fields.leader.classList.add('status-completed');
        fields.position.classList.add('status-completed');
    }
}

function stopAnimation() {
    if (animationTimer) {
        window.clearInterval(animationTimer);
        animationTimer = null;
    }
}

function reset() {
    stopAnimation();
    fields.leader.classList.remove('status-completed');
    fields.position.classList.remove('status-completed');
    lastResult = null;
    displayNodes = [];
    activeStep = null;
    setStatus('Not Started');
    fields.leader.textContent = '-';
    fields.position.textContent = '-';
    fields.rounds.textContent = '-';
    fields.messages.textContent = '-';
    fields.startTime.textContent = '-';
    fields.endTime.textContent = '-';
    fields.log.textContent = '';
    errorOutput.textContent = '';
    drawEmpty();
}

function syncCanvasSize() {
    const rect = canvas.getBoundingClientRect();
    const ratio = window.devicePixelRatio || 1;
    const width = Math.max(320, Math.round(rect.width));
    const height = Math.max(320, Math.round(rect.height));
    const pixelWidth = Math.round(width * ratio);
    const pixelHeight = Math.round(height * ratio);
    if (canvas.width !== pixelWidth || canvas.height !== pixelHeight) {
        canvas.width = pixelWidth;
        canvas.height = pixelHeight;
    }
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
    canvasWidth = width;
    canvasHeight = height;
}

function layout(result) {
    const width = canvasWidth;
    const height = canvasHeight;
    const footerSpace = 62;
    const compact = width < 620;
    const sideMargin = compact ? 46 : 90;
    const topMargin = compact ? 48 : 58;
    const bottomMargin = compact ? 82 : 95;
    const nodeSize = Math.max(42, Math.min(56, Math.floor((width - sideMargin * 2) / Math.max(result.cols, 1) * 0.74)));
    const graphWidth = Math.max(nodeSize, width - sideMargin * 2);
    const graphHeight = Math.max(nodeSize, height - footerSpace - bottomMargin - topMargin);
    const startX = (width - graphWidth) / 2;
    const startY = topMargin;
    const gapX = result.cols > 1 ? (graphWidth - nodeSize) / (result.cols - 1) : 120;
    const gapY = result.rows > 1 ? (graphHeight - nodeSize) / (result.rows - 1) : 90;
    return { nodeSize, startX, startY, gapX, gapY, x: col => startX + col * gapX, y: row => startY + row * gapY };
}

function draw(result, nodes, step) {
    syncCanvasSize();
    ctx.clearRect(0, 0, canvasWidth, canvasHeight);
    if (!result) {
        drawEmpty();
        return;
    }
    const grid = layout(result);
    drawLinks(result, grid);
    nodes.forEach(node => drawNode(node, grid, step));
    if (step) drawMessage(step, grid);
    drawFooter();
}

function drawLinks(result, grid) {
    ctx.strokeStyle = colors.link;
    ctx.lineWidth = 2;
    for (let row = 0; row < result.rows; row++) {
        for (let col = 0; col < result.cols; col++) {
            const x = grid.x(col) + grid.nodeSize / 2;
            const y = grid.y(row) + grid.nodeSize / 2;
            if (col < result.cols - 1) line(x + grid.nodeSize / 2, y, grid.x(col + 1), y);
            if (row < result.rows - 1) line(x, y + grid.nodeSize / 2, x, grid.y(row + 1));
        }
    }
    ctx.setLineDash([8, 7]);
    for (let row = 0; row < result.rows; row++) {
        const y = grid.y(row) + grid.nodeSize / 2;
        roundedArc(grid.x(0) - 42, y - 34, grid.x(result.cols - 1) - grid.x(0) + grid.nodeSize + 84, 68);
    }
    for (let col = 0; col < result.cols; col++) {
        const x = grid.x(col) + grid.nodeSize / 2;
        roundedArc(x - 34, grid.y(0) - 45, 68, grid.y(result.rows - 1) - grid.y(0) + grid.nodeSize + 90);
    }
    ctx.setLineDash([]);
}

function drawNode(node, grid, step) {
    let fill = node.leader ? colors.leaderFill : colors.processFill;
    let border = node.leader ? colors.leaderBorder : colors.processBorder;
    if (step && samePosition(node, step.senderPosition)) {
        fill = colors.senderFill;
        border = colors.senderBorder;
    } else if (step && samePosition(node, step.receiverPosition)) {
        fill = step.updated ? colors.receiverUpdatedFill : colors.receiverFill;
        border = colors.receiverBorder;
    }
    const x = grid.x(node.col);
    const y = grid.y(node.row);
    roundRect(x, y, grid.nodeSize, grid.nodeSize, 8, fill, border);
    ctx.fillStyle = colors.text;
    ctx.textAlign = 'center';
    const idFont = Math.max(17, Math.round(grid.nodeSize * 0.38));
    const metaFont = Math.max(9, Math.round(grid.nodeSize * 0.18));
    ctx.font = '700 ' + idFont + 'px Arial';
    ctx.fillText(String(node.id), x + grid.nodeSize / 2, y + grid.nodeSize * 0.58);
    ctx.font = metaFont + 'px Arial';
    ctx.fillText('max=' + node.maxKnownId, x + grid.nodeSize / 2, y + grid.nodeSize * 0.86);
}

function drawMessage(step, grid) {
    const sx = grid.x(step.senderPosition.col) + grid.nodeSize / 2;
    const sy = grid.y(step.senderPosition.row) + grid.nodeSize / 2;
    const rx = grid.x(step.receiverPosition.col) + grid.nodeSize / 2;
    const ry = grid.y(step.receiverPosition.row) + grid.nodeSize / 2;
    ctx.beginPath();
    ctx.fillStyle = '#dc2626';
    ctx.arc((sx + rx) / 2, (sy + ry) / 2, 15, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = 'white';
    ctx.font = '700 11px Arial';
    ctx.textAlign = 'center';
    ctx.fillText(String(step.transmittedValue), (sx + rx) / 2, (sy + ry) / 2 + 4);
}

function drawFooter() {
    const centerX = canvasWidth / 2;
    const footerTop = canvasHeight - 62;
    const separatorY = footerTop + 8;

    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, footerTop, canvasWidth, 62);

    ctx.lineWidth = 1;
    ctx.strokeStyle = '#d2e1f0';
    lineWithoutArrow(centerX - 180, separatorY, centerX - 35, separatorY);
    lineWithoutArrow(centerX + 35, separatorY, centerX + 180, separatorY);

    ctx.lineWidth = 2;
    ctx.strokeStyle = colors.link;
    lineWithoutArrow(centerX - 26, separatorY, centerX - 8, separatorY);
    lineWithoutArrow(centerX + 8, separatorY, centerX + 26, separatorY);

    ctx.fillStyle = '#0046a0';
    ctx.font = '700 13px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('Last row connects to first row', centerX, footerTop + 31);
    ctx.fillText('Last column connects to first column', centerX, footerTop + 51);
}

function drawEmpty() {
    syncCanvasSize();
    ctx.clearRect(0, 0, canvasWidth, canvasHeight);
    ctx.fillStyle = '#777777';
    ctx.font = '700 18px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    const lines = ['Click Run Election', 'or Auto Animate', 'to visualize the Torus Network'];
    lines.forEach((line, index) => ctx.fillText(line, canvasWidth / 2, canvasHeight / 2 - 26 + index * 28));
    ctx.textBaseline = 'alphabetic';
}

function samePosition(node, position) {
    return node.row === position.row && node.col === position.col;
}

function lineWithoutArrow(x1, y1, x2, y2) {
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
}

function line(x1, y1, x2, y2) {
    ctx.beginPath();
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();
    const angle = Math.atan2(y2 - y1, x2 - x1);
    const size = 7;
    ctx.beginPath();
    ctx.moveTo(x2, y2);
    ctx.lineTo(x2 - size * Math.cos(angle - Math.PI / 6), y2 - size * Math.sin(angle - Math.PI / 6));
    ctx.moveTo(x2, y2);
    ctx.lineTo(x2 - size * Math.cos(angle + Math.PI / 6), y2 - size * Math.sin(angle + Math.PI / 6));
    ctx.stroke();
}

function roundedArc(x, y, width, height) {
    ctx.beginPath();
    ctx.ellipse(x + width / 2, y + height / 2, Math.abs(width / 2), Math.abs(height / 2), 0, 0, Math.PI * 2);
    ctx.stroke();
}

function roundRect(x, y, width, height, radius, fill, stroke) {
    ctx.beginPath();
    ctx.roundRect(x, y, width, height, radius);
    ctx.fillStyle = fill;
    ctx.fill();
    ctx.strokeStyle = stroke;
    ctx.lineWidth = 2;
    ctx.stroke();
}

rowsInput.addEventListener('input', updateTotal);
colsInput.addEventListener('input', updateTotal);
window.addEventListener('resize', () => draw(lastResult, displayNodes, activeStep));
form.addEventListener('submit', event => {
    event.preventDefault();
    runElection().catch(error => errorOutput.textContent = error.message);
});
animateButton.addEventListener('click', () => {
    runElection({ animate: true }).catch(error => errorOutput.textContent = error.message);
});
resetButton.addEventListener('click', reset);
updateTotal();
drawEmpty();
