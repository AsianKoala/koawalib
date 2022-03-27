import matplotlib.pyplot as plt
import numpy as np
from math import sqrt

class MotionState:
    def __init__(self, x, v, a):
        self.x = x
        self.v = v
        self.a = a

    def calculate(self, dt):
        return MotionState(self.x + self.v * dt + 0.5 * self.a * dt ** 2, self.v + self.a * dt, self.a)

    def integrate(self, dt):
        return abs(self.v * dt + 0.5 * self.a * dt ** 2)
    
    def __str__(self) -> str:
        return 'x: {}, v: {}, a: {}'.format(self.x, self.v, self.a)

class MotionConstraints:
    def __init__(self, v_max, a_max, d_max, min_cruise_time=0):
        self.v_max = v_max
        self.a_max = a_max
        self.d_max = d_max
        self.min_cruise_time = min_cruise_time

    def __str__(self) -> str:
        return 'v_max: {}, a_max: {}, d_max: {}, min_cruise_time'.format(
            self.v_max, self.a_max, self.d_max, self.min_cruise_time)

class MotionProfile:
    def __init__(self, start_state: MotionState, end_state: MotionState, constraints: MotionConstraints):
        self.profileDuration = None
        self.accel_time = abs((constraints.v_max - start_state.v) / constraints.a_max)
        self.deccel_time = abs((end_state.v - constraints.v_max) / constraints.d_max)
        self.accel_state = MotionState(start_state.x, start_state.v, constraints.a_max)
        self.deccel_state = MotionState(end_state.x, end_state.v, -constraints.d_max).calculate(-self.deccel_time)

        accel_end_state = self.accel_state.calculate(self.accel_time)
        self.cruise_state = MotionState(accel_end_state.x, accel_end_state.v, 0)
        delta_x = end_state.x - start_state.x
        self.cruise_time = (delta_x - self.accel_state.integrate(self.accel_time) - 
            self.deccel_state.integrate(self.deccel_time)) / constraints.v_max

        if self.cruise_time < constraints.min_cruise_time:
            self.cruise_time = constraints.min_cruise_time
            constraints.a_max = self.max_abs(constraints.a_max, constraints.d_max)
            constraints.d_max = constraints.a_max
            self.accel_time = sqrt(abs(end_state.x)/abs(constraints.a_max))
            self.deccel_time = sqrt(abs(end_state.x)/abs(constraints.d_max))
            new_accel_end_state = self.accel_state.calculate(self.accel_time)
            self.deccel_state = MotionState(new_accel_end_state.x, new_accel_end_state.v, -constraints.d_max)

        self.profileDuration = self.accel_time + self.cruise_time + self.deccel_time
        self.total_integral = self.accel_state.integrate(self.accel_time) + self.cruise_state.integrate(self.cruise_time) + self.deccel_state.integrate(self.deccel_time)

    def get(self, time):
        if time <= self.accel_time:
            return self.accel_state.calculate(time)
        elif time <= self.accel_time + self.cruise_time:
            return self.cruise_state.calculate(time - self.accel_time)
        elif time <= self.profileDuration:
            return self.deccel_state.calculate(time - self.accel_time - self.cruise_time)
        else:
            return self.end_state

    def max_abs(self, a, b): return max(abs(a), abs(b))

def plot(profile):
    time = np.arange(0, profile.profileDuration, 0.01)
    states = np.array([profile.get(t) for t in time])
    x_values = [state.x for state in states]
    v_values = [state.v for state in states]
    a_values = [state.a for state in states]
    plt.plot(time, x_values, label='position')
    plt.plot(time, v_values, label='velocity')
    plt.plot(time, a_values, label='acceleration')
    plt.legend()
    plt.show()
    
def main():
    a_max = 16
    v_max = 40
    d_max = 16
    target = 80
    constraints = MotionConstraints(v_max, a_max, d_max)
    start_state = MotionState(0, 0, 0)
    end_state = MotionState(target, 0, 0)
    profile = MotionProfile(start_state, end_state, constraints)
    print('dt1: {}\ndt2: {}\ndt3: {}\nprofile duration {}\n integral: {}'.format(
        profile.accel_time, profile.cruise_time, profile.deccel_time, profile.profileDuration, profile.total_integral))
    plot(profile)

if __name__ == '__main__':
    main()