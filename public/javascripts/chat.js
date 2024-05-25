const inputField = document.getElementById("chat-input");
const outputArea = document.getElementById("chat-area");
const userName = document.getElementById("user-name")
const submitButton = document.getElementById("submit-name")


submitButton.addEventListener("click", () => {
    if (userName.value !== "") {
        submitButton.disabled = true
        userName.readOnly = true
        inputField.disabled = false
        socket.send( getCurrentDateUTC()  + userName.value + " just joined the chat");
    }
});

function getCurrentDateUTC() {
    return "[" + new Date().toUTCString() + "] "
}

const socketRoute = document.getElementById("ws-route").value;
const socket = new WebSocket(socketRoute.replace("http","ws"));

inputField.onkeydown = (event) => {
    if(event.key === 'Enter') {
        socket.send(getCurrentDateUTC() +  userName.value + " - " + inputField.value);
        inputField.value = '';
    }
}

socket.onmessage = (event) => {
    outputArea.value += '\n' + event.data;
}