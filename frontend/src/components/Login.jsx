import { useState } from 'react'

const API = "http://localhost:8080"

function Login({ onLogin }) {
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [error, setError] = useState("")
    const [loading, setLoading] = useState(false)

    const handleLogin = async (e) => {
        e.preventDefault()
        setError("")
        setLoading(true)

        try {

            const csrfResponse = await fetch(
                `${API}/api/auth/csrf`,
                {
                    credentials: 'include'
                }
            )

            if(!csrfResponse.ok) {
                throw new Error("Could not obtain security token.")
            }

            const csrfData = await csrfResponse.json()

            const formData = new URLSearchParams()

            formData.append('username', username)
            formData.append('password', password)

            const response = await fetch(
                `${API}/login`,
                {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'X-XSRF-TOKEN': csrfData.token,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: formData,
                }
            );

            if(response.ok) {
                onLogin()
            } else if(response.status === 401) {
                setError("Invalid Credentials")
            } else {
                setError(`Login failed. Status: ${response.status}`)
            }

        } catch (err) {
            setError(err.message || "Unable to connect to the server.")
        } finally {
            setLoading(false)
        }

    }

    return (

        <div className="auuth-page">

            <form className="auth-form" onSubmit={handleLogin}>

                <h1>Login to TradeLens</h1>

                <label htmlFor="login-username">Username</label>

                <input
                    id="login-username"
                    type="text"
                    autoComplete="username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />

                <label htmlFor="login-password">Password</label>
                <input
                    id="login-password"
                    type="password"
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}

                />

                { error &&
                    <p role="alert">
                        {error}
                    </p>
                }


                <button type="submit" disabled={loading}>
                    { loading ? "Logging in..." : "Login" }
                </button>

                <p>
                    Don't have an account? <a href="/signup">Sign up</a>
                </p>

            </form>




        </div>
    )
}

export default Login