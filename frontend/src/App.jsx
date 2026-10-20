import MarketContext from './components/MarketContext'
import Behavior from "./components/Behavior.jsx";
import Strategies from "./components/Strategies";
import Analytics from './components/Analytics'
import TradeDetails from './components/TradeDetails'
import Trades from './components/Trades'
// import Header from './components/Header'
import Sidebar from './components/Sidebar'
import Dashboard from './components/Dashboard'
import Login from './components/Login'
import Signup from "./components/Signup";
import './App.css'


import {useEffect, useState} from "react";

import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
    useNavigate,
    useLocation,
    useParams,
} from 'react-router-dom'

function TradeDetailsPage() {
    const {tradeId} = useParams()

    return <TradeDetails tradeId={tradeId} />
}

function ProtectedApp({ onLogout, role }) {

    const navigate = useNavigate();
    const location = useLocation();

    const currentPage = location.pathname.startsWith('/trades/')
        ? 'trade-details'
        : location.pathname.slice(1) || 'dashboard'

    return (

        <div className="app">
            <div className="app-body">

                <Sidebar
                    onNavigate={(nextPage) => navigate(`/${nextPage}`)}
                    currentPage={currentPage}
                    onLogout={onLogout}
                />

                <Routes>
                    <Route
                        path="/"
                        element={<Navigate to="/dashboard" replace />}
                    />

                    <Route
                        path="/dashboard"
                        element={<Dashboard />}
                    />

                    <Route
                        path="/trades"
                        element={
                            <Trades
                                role={role}
                                onSelectTrade={(tradeId) => {
                                    navigate(`/trades/${tradeId}`)
                                }}
                            />
                        }
                    />

                    <Route
                        path="/trades/:tradeId"
                        element={<TradeDetailsPage/>}
                    />


                    <Route
                        path="/analytics"
                        element={<Analytics />}
                    />

                    <Route
                        path="/strategies"
                        element={<Strategies />}
                    />

                    <Route
                        path="/behavior"
                        element={<Behavior />}
                    />

                    <Route
                        path="/market-context"
                        element={<MarketContext />}
                    />

                    <Route
                        path="*"
                        element={<Navigate to="/dashboard" replace />}
                    />

                </Routes>

            </div>
        </div>

    )

}

function getCookie(name) {

    const prefix = name + "="

    const cookie = document.cookie
        .split("; ")
        .find((item) => item.startsWith(prefix))

    return cookie ? decodeURIComponent(cookie.substring(prefix.length)) : ""

}

function AppContent() {

    const [isLoggedIn, setIsLoggedIn] = useState(null);
    const [role, setRole] = useState(null);
    const [checkingSession, setCheckingSession] = useState(true);

    const navigate = useNavigate();
    const location = useLocation();

    const loadCurrentUser = async () => {
        const response = await fetch(
            "http://localhost:8080/api/auth/me",
            {
                credentials: "include"
            }
        )

        if(!response.ok) {
            throw new Error("Unable to load current user.");
        }

        const userData = await response.json();

        setIsLoggedIn(true);
        setRole(userData.role);

    }

    useEffect(() => {

        let active = true;

        fetch('http://localhost:8080/api/auth/me',
            {
                credentials: 'include'
            })
            .then(async (response) => {

                if(!response.ok) {
                    throw new Error("Not authenticated");
                }

                const userData = await response.json();

                if (active) {
                    setIsLoggedIn(response.ok);
                    setRole(userData.role);
                }

            })
            .catch(() => {
                if (active) {
                    setIsLoggedIn(false);
                    setRole(null);
                }
            })
            .finally(() => {
                if (active) {
                    setCheckingSession(false);
                }
            });

        return () => {
            active = false;
        }

    }, [])

    if (checkingSession) {
        return <p>Checking login...</p>
    }


    const isAuthPage =
        location.pathname === "/login" ||
        location.pathname === "/signup"

    if (!isLoggedIn) {
        if(location.pathname === "/signup")
        {
            return (
                <Signup
                    onSignup={() => navigate("/login", {replace: true})}
                />
            )
        }

        if (location.pathname !== "/login") {
            return <Navigate to="/login" replace/>
        }

        return (
            <Login
                onLogin={async () => {

                    try {
                        await loadCurrentUser()
                        navigate("/dashboard", { replace: true })

                    } catch (err) {
                        console.error(err)
                        setIsLoggedIn(false)
                        setRole(null)
                    }

                }}
            />
        )
    }

    if (isAuthPage) {
        return <Navigate to="/dashboard" replace/>
    }

    const handleLogout = async () => {

        try {

            const csrfResponse = await fetch(
                'http://localhost:8080/api/auth/csrf',
                {
                    credentials: 'include'
                }
            )

            if(!csrfResponse.ok) {
                throw new Error("Could not obtain CSRF token.")
            }

            const csrfData = await csrfResponse.json();

            const response = await fetch(
                "http://localhost:8080/logout",
                {
                    method: "POST",
                    credentials: 'include',
                    headers: {
                        "X-XSRF-TOKEN": csrfData.token,
                    },
                }
            );

            if(!response.ok) {
                throw new Error("Logout failed.")
            }

            setIsLoggedIn(false)
            navigate("/login", { replace: true })

        } catch (err) {
            console.error(err)
            alert("Could not log out. Please try again.")
        }
    }


    return <ProtectedApp
        onLogout={handleLogout}
        role={role}
    />

}

function App() {

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/*" element={<AppContent />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App