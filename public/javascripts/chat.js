const inputField = document.getElementById("chat-input");
const outputArea = document.getElementById("chat-area");
const userName = document.getElementById("user-name")


function getCurrentDateUTC() {
    return "[" + new Date().toUTCString() + "] "
}

const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http","ws"));

setTimeout(() => {
    socket.send( getCurrentDateUTC()  + userName.value + " just joined the chat");
}, 500);


inputField.onkeydown = (event) => {
    if(event.key === 'Enter' && inputField.value.trim() !== "") {
        const jwtToken = document.cookie

        const message = getCurrentDateUTC() +  userName.value.trim() + " - " + inputField.value.trim()
        const msgWithToken = JSON.stringify({ msg: message, jwtToken: jwtToken });

        socket.send(msgWithToken);
        inputField.value = '';
    }
}

socket.onmessage = (event) => {
    console.log(event.data)
    try {
        const parsed = JSON.parse(event.data)

        if ("action" in parsed) {
            if (parsed.action === "refresh") {
                location.reload()
            }
        }
        return
    } catch (e) {
        console.info(`"${event.data}" is not a valid JSON`)
    }

    outputArea.value += '\n' + event.data;
}