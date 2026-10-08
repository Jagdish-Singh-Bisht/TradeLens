import { useState } from 'react'

function Login({ onLogin }) {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')

    const handleLogin = async (e) => {
        e.preventDefault()

        const csrfResponse = await fetch(
            'http://localhost:8080/api/auth/csrf',
            {
                credentials: 'include'
            }
        )

        const csrfData = await csrfResponse.json()

        const formData = new URLSearchParams()

        formData.append('username', username)
        formData.append('password', password)

        const response = await fetch(
            'http://localhost:8080/login',
            {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'X-XSRF-TOKEN': csrfData.token,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData
            }
        )

        console.log('Login status:', response.status)

        if(response.ok) {
            onLogin()
        }

        console.log('Login URL:', response.url)

        // const analyticsResponse = await fetch(
        //     'http://localhost:8080/api/broker-accounts/1/analytics',
        //     {
        //         credentials:'include'
        //     }
        // )

    }

    return (
        <div>
            <h2>Login</h2>

            <input
                type="text"
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />

            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />

            <button onClick={handleLogin}>Login</button>

        </div>
    )
}

export default Login