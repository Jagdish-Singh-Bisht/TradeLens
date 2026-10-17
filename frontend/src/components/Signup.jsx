import { useState } from "react"

const API = "http://localhost:8080"

function getCookie(name) {
    const prefix = name + "="

    const cookie = document.cookie
        .split("; ")
        .find((item) => item.startsWith(prefix))

    return cookie ? decodeURIComponent(cookie.substring(prefix.length)) : ""

}

export default function Signup({ onSignup }) {

    const [username, setUsername] = useState("")
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [error, setError] = useState("")
    const [loading, setLoading] = useState(false)

    async function handleSignup(event) {

        event.preventDefault()
        setError("")
        setLoading(true)

        try {

            await fetch(`${API}/api/auth/csrf`,
                {
                    credentials: 'include'
                })

            const csrfToken = getCookie("XSRF-TOKEN")

            if(!csrfToken) {
                throw new Error("Could not obtain security token. Please retry. ")
            }

            const response = await fetch(`${API}/api/auth/register`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    "X-XSRF-TOKEN": csrfToken,
                },
                body: JSON.stringify({username, email, password}),
            })

            const data = await response.json()
                .catch(() => (
                    { }
                ))

            if(!response.ok) {
                throw new Error(data.message || "Registration failed.")
            }

            onSignup()

        } catch(err) {
            setError(err.message || "Could not connect to the server.")
        } finally {
            setLoading(false)
        }

    }

    return (

        <div className="auth-page">

            <form className="auth-form" onSubmit={handleSignup}>

                <h1>Create your TradeLens account</h1>
                <p>Signup to start tracking your trading performance.</p>

                <label htmlFor="username">Username</label>

                <input
                    id="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    autoComplete="username"
                    required
                />

                <label htmlFor="email">Email</label>

                <input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="email"
                    required
                />

                <label htmlFor="password">Password</label>

                <input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password"
                    minLength={8}
                    required
                />

                { error &&
                    <p role="alert">
                        {error}
                    </p>
                }

                <button type="submit" disabled={loading}>
                    { loading ? "Creating account..." : "Sign up" }
                </button>

                <p>
                    Already registered? <a href="/login">Log in</a>
                </p>

            </form>
        </div>
    )
}