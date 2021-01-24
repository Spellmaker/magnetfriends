console.log("initializing application");

let allViews = document.querySelectorAll(".appview");

let baseUrl = "https://magnets.shittyidle.com/api";

allViews.forEach(a => a.classList.add("hidden"));

let userId = getCookie("magnetfriends_id");
let secret = getCookie("magnetfriends_secret");
let userName = getCookie("magnetfriends_name");
let roomid = null;

if (userId === "") {
    showView("#login");
} else {
    checkCookiesValid();
}

function showView(name) {
    allViews.forEach(a => a.classList.add("hidden"));
    document.querySelector(name).classList.remove("hidden");
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function login() {
    userId = document.querySelector("#username").value;
    secret = document.querySelector("#secret").value;

    fetch(baseUrl + "/user", {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            name: userId,
            secret: secret
        })
    })
        .then(response => {
            if (response.status === 200) {
                checkCookiesValid();
            } else {
                showMessage("Invalid user credentials")
                showView("#login");
            }
        })

    checkCookiesValid();
}

function checkCookiesValid() {
    fetch(baseUrl + "/user")
        .then(response => {
            if (response.status === 200) {
                showMessage("User is online as " + userId);
                showView("#roomselection");
            } else {
                showMessage("Login required");
                showView("#login");
            }
        });
}

let messageTimeout = null;

function showMessage(message) {
    if (messageTimeout != null) {
        clearTimeout(messageTimeout);
    }
    let messageField = document.querySelector("#message");

    messageField.textContent = message;

    messageTimeout = setTimeout(function () {
        messageField.classList.add("hidden");
    }, 2000);
}

function createRoom() {
    let roomField = document.querySelector("#roomfield").value;
    fetch(baseUrl + "/room", {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: roomField
        }
    )
        .then(response => response.text())
        .then(data => {
            roomid = data;
            startRound();
        })
        .catch(_ => showMessage("Could not create room"));
}

function joinRoom() {
    console.log("ininitate room join");
    let roomField = document.querySelector("#roomfield").value;
    fetch(baseUrl + "/room/" + roomField, {
            method: 'POST'
        }
    )
        .then(response => response.text())
        .then(data => {
                console.log("joined room");
                roomid = roomField;
                initRoomView();
            }
        )
        .catch(_ => showMessage("Could not join room"));
}

let roomtimeout = null;
function initRoomView() {
    console.log("init room view");
    showView("#gameview");
    document.querySelector("#display-user").textContent = userName;
    document.querySelector("#display-room-code").textContent = roomid;

    fetch(baseUrl + "/room/" + roomid)
        .then(response => response.json())
        .then(roomData => {
            fetch(baseUrl + "/room/" + roomid + "/snippet")
                .then(response => response.json())
                .then(snippetData => {
                    console.debug(roomData);
                    document.querySelector("#display-room").textContent = roomData.name;
                    document.querySelector("#display-room-round").textContent = roomData.currentRound;
                    updateMembers(roomData);
                    console.log("update shown");
                    updateShown(roomData);
                    console.log("update stats");
                    updateSnippetStatistic(roomData, snippetData);
                    updateMySnippets(snippetData);
                    if (roomtimeout != null) {
                        clearTimeout(roomtimeout);
                        roomtimeout = null;
                    }
                    roomtimeout = setTimeout(function () { initRoomView() }, 1000);
                })
                .catch(_ => showMessage("Error fetching snippet details"))
        })
        .catch(_ => showMessage("Error fetching room details"))
}

function updateMembers(roomData) {
    console.log("updating members");
    console.log("data: ");
    console.debug(roomData);
    /* build members */
    let memberList = "<ul>";
    roomData['members'].forEach((member) => {
        if (member.id === roomData['owner'].id) {
            memberList += "<li class=\"owner\">" + member.name + "</li>";
        } else {
            memberList += "<li>" + member.name + "</li>";
        }
    });
    memberList += "</ul>";
    document.querySelector("#members").innerHTML = memberList
}

function updateShown(roomData) {
    let content = "";
    if (roomData['shownSnippets'] != null) {
        roomData.shownSnippets.forEach((snippetDTO) => {
            content += shownSnippetContent(snippetDTO);
        })
    }
    document.querySelector("#shownsnippets")
        .innerHTML = content;
}

function updateSnippetStatistic(roomData, snippetData) {
    let snippetStats = document.querySelector("#snippetstats")
    snippetStats.innerHTML = "<p>Total snippets this round: " + roomData.totalSnippets + "</p>"
        + "<p>Snippets submitted by you this round: " + snippetData.yourSuppliedSnippets + "</p>"
        + "<p>Unclaimed snippets: " + snippetData.unclaimedSnippets + "</p>";
}

function updateMySnippets(snippetData) {
    console.log("updating your snippets");
    let content = "";
    if (snippetData['yourHeldSnippets'] != null) {
        console.log("looking actually at snippets");
        snippetData.yourHeldSnippets.forEach((snippetDTO) => {
            console.log("dto is");
            console.debug(snippetDTO);
            content += mySnippetContent(snippetDTO);
        })
    }
    document.querySelector("#snippetarea")
        .innerHTML = content;
}

function shownSnippetContent(snippetDTO) {
    return "<div class=\"shownsnippet\">" + snippetDTO.content + "</div>";
}

function mySnippetContent(snippetDTO) {
    let content = "";
    if (snippetDTO.used) {
        content += "<div class=\"heldsnippet usedsnippet\"><div class='snippetlabel'>";
        content += snippetDTO.content + "</div><div class='snippetbutton'></div>";
    } else {
        content += "<div class=\"heldsnippet\"><div class='snippetlabel'>"
        content += snippetDTO.content;
        content += "</div><div class='snippetbutton'><button type=\"button\" onClick=\"useSnippet(" + snippetDTO.id + ")\">Use snippet</button></div>";
    }
    content += "</div>";
    console.log("content is: " + content);
    return content;
}

function startRound() {
    fetch(baseUrl + "/room/" + roomid + "/round", {
        method: 'POST'
    }).catch(_ => showMessage("Error starting round"))
        .then(_ => initRoomView());
}

function hideSnippets() {
    fetch(baseUrl + "/room/" + roomid + "/snippet/hide", {
        method: 'POST'
    }).catch(_ => showMessage("Error hiding snippets"))
        .then(_ => initRoomView());
}

function drawSnippet() {
    let snippetCount = document.querySelector("#snippetdrawcount").value;
    fetch(baseUrl + "/room/" + roomid + "/snippet/draw?count=" + snippetCount)
        .then(_ => initRoomView())
        .catch(_ => showMessage("Error drawing snippets"));
}

function useSnippet(snippetId) {
    fetch(baseUrl + "/room/" + roomid + "/snippet/" + snippetId, {
        method: 'POST'
    }).catch(_ => showMessage("Error using snippet"))
        .then(_ => initRoomView());
}

function addSnippet() {
    let snippet = document.querySelector("#newsnippet").value;
    fetch(baseUrl + "/room/" + roomid + "/snippet", {
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        body: snippet
    })
        .then(_ => {
            document.querySelector("#newsnippet").value = "";
            initRoomView()
        })
        .catch(_ => showMessage("Error submitting snippet"));

}