"""MCPyLib - Minecraft Python Library

A Python library for controlling Minecraft servers remotely.
"""

from .client import (
    MCPyLib,
    MCPyLibError,
    ConnectionError,
    AuthenticationError,
    CommandError,
)

__version__ = "1.0.2"
__all__ = [
    "MCPyLib",
    "MCPyLibError",
    "ConnectionError",
    "AuthenticationError",
    "CommandError",
]
