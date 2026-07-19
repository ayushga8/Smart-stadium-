import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginCard from '../components/LoginCard';

// Mock the API module
vi.mock('../api', () => ({
  requestOtp: vi.fn(),
  verifyOtp: vi.fn(),
  googleLogin: vi.fn(),
}));

import { requestOtp, verifyOtp, googleLogin } from '../api';

describe('LoginCard', () => {
  let user;

  beforeEach(() => {
    user = userEvent.setup();
    vi.clearAllMocks();
  });

  // Helper: switch to email tab (Google is default now)
  const switchToEmail = async () => {
    await user.click(screen.getByRole('tab', { name: /email otp/i }));
  };

  it('should render email OTP tab and Google tab', () => {
    render(<LoginCard />);
    expect(screen.getByRole('tab', { name: /email otp/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /google/i })).toBeInTheDocument();
  });

  it('should show Google button by default', () => {
    render(<LoginCard />);
    expect(screen.getByText('Continue with Google')).toBeInTheDocument();
  });

  it('should show email input after switching to Email OTP tab', async () => {
    render(<LoginCard />);
    await switchToEmail();
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByText('Send Code')).toBeInTheDocument();
  });

  it('should show email validation error for invalid email', async () => {
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'not-valid');
    await user.click(screen.getByText('Send Code'));
    expect(screen.getByText('Invalid email format')).toBeInTheDocument();
  });

  it('should show required error for empty email', async () => {
    render(<LoginCard />);
    await switchToEmail();
    await user.click(screen.getByText('Send Code'));
    expect(screen.getByText('Email is required')).toBeInTheDocument();
  });

  it('should call requestOtp and show OTP step', async () => {
    requestOtp.mockResolvedValue(true);
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => {
      expect(requestOtp).toHaveBeenCalledWith('test@example.com');
      expect(screen.getByText(/enter the 6-digit code/i)).toBeInTheDocument();
    });
  });

  it('should show error when requestOtp fails', async () => {
    requestOtp.mockRejectedValue({ message: 'Too many requests' });
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => {
      expect(screen.getByText('Too many requests')).toBeInTheDocument();
    });
  });

  it('should show spinner when loading', async () => {
    requestOtp.mockImplementation(() => new Promise(() => {})); // never resolves
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => {
      expect(screen.queryByText('Send Code')).not.toBeInTheDocument();
      expect(document.querySelector('.spinner')).toBeInTheDocument();
    });
  });

  it('should have Verify button disabled until 6 digits entered', async () => {
    requestOtp.mockResolvedValue(true);
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => {
      expect(screen.getByText('Verify & Enter')).toBeDisabled();
    });
  });

  it('should show Continue with Google button on default Google tab', () => {
    render(<LoginCard />);
    expect(screen.getByText('Continue with Google')).toBeInTheDocument();
  });

  it('should call googleLogin when Google button is clicked', async () => {
    render(<LoginCard />);
    await user.click(screen.getByText('Continue with Google'));
    expect(googleLogin).toHaveBeenCalled();
  });

  it('should clear email when switching tabs', async () => {
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');

    // Switch to Google and back
    await user.click(screen.getByRole('tab', { name: /google/i }));
    await user.click(screen.getByRole('tab', { name: /email otp/i }));

    expect(screen.getByLabelText(/email address/i)).toHaveValue('');
  });

  it('should show resend timer after sending OTP', async () => {
    requestOtp.mockResolvedValue(true);
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => {
      expect(screen.getByText(/resend code/i)).toBeInTheDocument();
      expect(screen.getByText(/in \d+s/)).toBeInTheDocument();
    });
  });

  it('should disable resend button during countdown', async () => {
    requestOtp.mockResolvedValue(true);
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => {
      expect(screen.getByText(/resend code/i)).toBeDisabled();
    });
  });

  it('should show back button in OTP step', async () => {
    requestOtp.mockResolvedValue(true);
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => {
      expect(screen.getByText(/back to email/i)).toBeInTheDocument();
    });
  });

  it('should go back to email step when back button is clicked', async () => {
    requestOtp.mockResolvedValue(true);
    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => screen.getByText(/back to email/i));
    await user.click(screen.getByText(/back to email/i));

    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
  });

  it('should display error from verify endpoint', async () => {
    requestOtp.mockResolvedValue(true);
    verifyOtp.mockRejectedValue({ message: 'Invalid OTP code' });

    render(<LoginCard />);
    await switchToEmail();
    const input = screen.getByLabelText(/email address/i);
    await user.type(input, 'test@example.com');
    await user.click(screen.getByText('Send Code'));

    await waitFor(() => screen.getAllByRole('textbox'));

    // Type OTP digits
    const otpInputs = screen.getAllByRole('textbox');
    for (let i = 0; i < 6; i++) {
      await user.click(otpInputs[i]);
      await user.keyboard(String(i + 1));
    }

    await user.click(screen.getByText('Verify & Enter'));

    await waitFor(() => {
      expect(screen.getByText('Invalid OTP code')).toBeInTheDocument();
    });
  });
});
