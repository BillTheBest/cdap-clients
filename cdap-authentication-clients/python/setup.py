# -*- coding: utf-8 -*-

#  Copyright © 2014 Cask Data, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.

from setuptools import setup
from setuptools import find_packages

setup(name='cdap-auth-client',
    version='${project.version}',
    description='Authentication client for Cask Data Application Platform',
    author='Cask Data',
    license='The Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0',
    author_email='cask-dev@googlegroups.com',
    install_requires=["six"],
    packages=find_packages(),
    )

