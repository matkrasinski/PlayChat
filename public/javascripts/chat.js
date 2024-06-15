// UI CONSTANTS HTML ELEMENTS
const inputField = document.getElementById("chat-input");
const outputArea = document.getElementById("chat-area");
const userName = document.getElementById("user-name")
const logoutButton = document.getElementById("logout-button")

const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http","ws"));


// UTILITY FUNCTIONS
function getCurrentDateUTC(date = new Date()) {
    date.setHours(date.getHours() + 2)
    return "[" + date.toUTCString() + "] "
}

function formatMessage(data) {
    return getCurrentDateUTC(data.dateTime) + data.username + " - " + data.msg
}

function clearAllCookies() {
    const cookies = document.cookie.split(";");

    cookies.forEach(cookie => {
        const eqPos = cookie.indexOf("=");
        const name = eqPos > -1 ? cookie.slice(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/";
    });
}


// ON ACTION

setTimeout(() => {
    socket.send( getCurrentDateUTC()  + userName.value + " just joined the chat");
}, 500);


inputField.onkeydown = (event) => {
    if(event.key === 'Enter' && inputField.value.trim() !== "") {
        const jwtToken = document.cookie
        const msgWithToken = JSON.stringify({
            username: userName.value,
            msg: inputField.value.trim(),
            jwtToken: jwtToken
        });

        socket.send(msgWithToken);
        inputField.value = '';
    }
}

logoutButton.onclick = () => {
    clearAllCookies()
    location.reload()
}

socket.onmessage = (event) => {
    console.log(event.data)
    try {
        const parsed = JSON.parse(event.data)

        if ("action" in parsed) {
            if (parsed.action === "refresh") {
                location.reload()
                return
            }
        }
        outputArea.value += '\n' + formatMessage(parsed);
    } catch (e) {
        console.info(`"${event.data}" is not a valid JSON`)
    }
}