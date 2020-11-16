let elements = document.getElementsByTagName("time")

for (let i = 0; i < elements.length; i++) {
	let node = elements[i];
	node.innerHTML = new Date(Date.parse(node.getAttribute("datetime"))).toLocaleString()
}
