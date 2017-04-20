from setuptools import setup, find_packages
from pip.req import parse_requirements

install_requirements = parse_requirements('requirements.txt', session=False)
requirements = [str(ir.req) for ir in install_requirements]
setup(
    name='webhook_code_samples',
    version='1.0',
    description='Code Samples to integrate Helpshift webhooks with Helpshift API, Jira, Slack.',
    author='Pardeep Singh',
    author_email='pardeep@helpshift.com',
    install_requires=requirements,
    packages=find_packages()
)
