import { useState, useCallback, useRef, useEffect } from 'react';
import OtpInput from './OtpInput';
import { requestOtp, verifyOtp, googleLogin } from '../api';

export default function LoginCard() {
  const [tab, setTab] = useState('google'); // 'google' | 'email'
  const [step, setStep] = useState('email'); // 'email' | 'otp'
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resendTimer, setResendTimer] = useState(0);
  const [emailError, setEmailError] = useState('');
  const timerRef = useRef(null);

  // Clear state when switching tabs
  useEffect(() => {
    setStep('email');
    setEmail('');
    setOtp('');
    setError('');
    setEmailError('');
    setLoading(false);
    if (timerRef.current) clearInterval(timerRef.current);
    setResendTimer(0);
  }, [tab]);

  // Cleanup timer on unmount
  useEffect(() => () => { if (timerRef.current) clearInterval(timerRef.current); }, []);

  const startResendTimer = () => {
    setResendTimer(30);
    if (timerRef.current) clearInterval(timerRef.current);
    timerRef.current = setInterval(() => {
      setResendTimer(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const validateEmail = (val) => {
    if (!val) return 'Email is required';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val)) return 'Invalid email format';
    return '';
  };

  const handleSendCode = async () => {
    const err = validateEmail(email);
    if (err) { setEmailError(err); return; }
    setEmailError('');
    setError('');
    setLoading(true);

    try {
      await requestOtp(email);
      setStep('otp');
      startResendTimer();
    } catch (e) {
      setError(e.message || 'Failed to send code');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async () => {
    if (otp.length < 6) return;
    setError('');
    setLoading(true);

    try {
      const data = await verifyOtp(email, otp);
      // Store token — backend may return accessToken or token
      const token = data.accessToken || data.token;
      if (token) {
        localStorage.setItem('accessToken', token);
        // Use replace + reload to ensure App re-runs routing with token
        window.history.replaceState(null, '', '/dashboard');
        window.location.reload();
      } else {
        setError('No token received from server');
      }
    } catch (e) {
      setError(e.message || 'Verification failed');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (resendTimer > 0) return;
    setError('');
    setLoading(true);
    try {
      await requestOtp(email);
      startResendTimer();
    } catch (e) {
      setError(e.message || 'Failed to resend code');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpComplete = useCallback((code) => {
    setOtp(code);
  }, []);

  return (
    <div className="login-card">
      <h2>Welcome Back</h2>
      <p className="subtitle">Sign in to your Smart Stadium account</p>

      {/* Tab Toggle */}
      <div className="tab-bar" role="tablist">
        <button
          role="tab"
          aria-selected={tab === 'google'}
          className={tab === 'google' ? 'active' : ''}
          onClick={() => setTab('google')}
        >
          🔵 Google
        </button>
        <button
          role="tab"
          aria-selected={tab === 'email'}
          className={tab === 'email' ? 'active' : ''}
          onClick={() => setTab('email')}
        >
          ✉ Email OTP
        </button>
      </div>

      {/* Error */}
      {error && (
        <div className="error-text" role="alert">
          {error}
        </div>
      )}

      {tab === 'google' && (
        <>
          <button
            className="btn-google"
            onClick={() => googleLogin()}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
            Continue with Google
          </button>
          <div className="divider">or use email</div>
        </>
      )}

      {tab === 'email' && step === 'email' && (
        <>
          <div className="form-group">
            <label htmlFor="email-input">Email Address</label>
            <input
              id="email-input"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={e => { setEmail(e.target.value); setEmailError(''); }}
              onKeyDown={e => e.key === 'Enter' && handleSendCode()}
              disabled={loading}
              autoComplete="email"
            />
            {emailError && <div className="error-text">{emailError}</div>}
          </div>
          <button
            className="btn-primary"
            onClick={handleSendCode}
            disabled={loading}
          >
            {loading ? '⏳ Sending...' : 'Send Code'}
          </button>
        </>
      )}

      {tab === 'email' && step === 'otp' && (
        <>
          <button
            className="resend-btn"
            onClick={() => setStep('email')}
            style={{ marginBottom: '0.75rem', display: 'block', textDecoration: 'none', color: '#7df4ff', fontSize: '0.85rem' }}
          >
            ← Back to email
          </button>
          <p style={{ textAlign: 'center', fontSize: '0.85rem', color: '#b9cacb', marginBottom: '0.5rem' }}>
            Enter the 6-digit code sent to<br />
            <strong style={{ color: '#7df4ff' }}>{email}</strong>
          </p>
          <OtpInput onComplete={handleOtpComplete} disabled={loading} />
          <button
            className="btn-primary"
            onClick={handleVerify}
            disabled={otp.length < 6 || loading}
          >
            {loading ? '⏳ Verifying...' : 'Verify & Enter'}
          </button>
          <div className="resend-timer">
            <button
              className="resend-btn"
              onClick={handleResend}
              disabled={resendTimer > 0 || loading}
            >
              Resend code
            </button>
            {resendTimer > 0 && (
              <span style={{ marginLeft: '0.25rem' }}>
                in {resendTimer}s
              </span>
            )}
          </div>
        </>
      )}
    </div>
  );
}
